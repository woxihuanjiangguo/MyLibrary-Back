package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.domain.book.Copy;
import fudan.se.lab3.domain.people.Code;
import fudan.se.lab3.domain.people.PayLog;
import fudan.se.lab3.domain.people.Reader;
import fudan.se.lab3.repository.*;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import fudan.se.lab3.common.Utils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class ReaderService {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private final ReaderRepository readerRepository;
    private final AuthorityRepository authorityRepository;
    private final AttributesRepository attributesRepository;
    private final CodeRepository codeRepository;
    private final CopyRepository copyRepository;
    private final PayLogRepository payLogRepository;

    private static final String SendFrom = "woxihuanruanjian@163.com";
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String invoke_id = "se2021_17";
    private static final String paymentUrl = "http://47.103.205.96:8080/api/payment";

    @Autowired
    public ReaderService(ReaderRepository readerRepository,
                         AuthorityRepository authorityRepository,
                         AttributesRepository attributesRepository,
                         CodeRepository codeRepository,
                         CopyRepository copyRepository,
                         PayLogRepository payLogRepository) {
        this.readerRepository = readerRepository;
        this.authorityRepository = authorityRepository;
        this.attributesRepository = attributesRepository;
        this.codeRepository = codeRepository;
        this.payLogRepository = payLogRepository;
        this.copyRepository = copyRepository;
    }

    public Map<String, Object> code(CodeRequest request){
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail();
        if(codeRepository.findByEmail(email) != null){
            response.put("message", "验证码已经发送到您的邮箱了，请勿重复发送");
            return response;
        }

        // send mail here
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("您的 MyLibrary 注册验证码");
        message.setFrom(SendFrom);    // Do not modify
        message.setTo(email);
        message.setSentDate(new Date());

        try{
            // deal with sending email
            String code = Utils.codeGenerator();
            String msg = "您的验证码为：\n" + code;
            message.setText(msg);
            javaMailSender.send(message);
            this.codeRepository.save(new Code(code,email));
            response.put("message", "success");
        }catch (NoSuchAlgorithmException e){
            response.put("message","Unknown error");
        }catch (Exception e){
            // handle nonexistent email address
            response.put("message", "Mail sender is unexpectedly blocked");
        }
        return response;
    }

    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        String userType = request.getReaderType();
        String securityCode = request.getSecurityCode();
        Code codeInRepo = codeRepository.findByEmail(email);

        if (readerRepository.findByUsername(username) != null) {
            response.put("message","Username" + username + " has been registered");
        } else if (Utils.registerIsInvalid(request)) {
            response.put("message","Invalid format of your register info");
        } else if (codeInRepo == null || !codeInRepo.getCode().equals(securityCode)) {
            response.put("message","Your security code is invalid");
        } else{
            if(codeRepository.existsAllByEmail(email)){
                codeRepository.deleteAllByEmail(email);
            }
            Reader reader = new Reader(username, (new BCryptPasswordEncoder()).encode(password), email,
                    new HashSet<>(Collections.singletonList(authorityRepository.findByAuthority("Reader"))),
                    attributesRepository.findByType(userType));
            readerRepository.save(reader);
            response.put("message", "Register success!");
            response.put("id", reader.getId());
        }
        return response;
    }

    public Map<String, Object> payFine(String token, String cid){
        Map<String, Object> response = new HashMap<>();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Reader reader = this.readerRepository.findByUsername(username);
        String payTime = format.format(new Date());
        // invalid user part
        if (reader == null) {
            response.put("message","Reader does not exist!");
            return response;
        }
        // valid user part
        // 支付逻辑部分 --- 获取信息 -> 调用助教接口 -> payLog（成功）/response加信息
        Copy copy = copyRepository.findByCid(cid);
        // 没这本书，或者用户不该买单
        if (copy == null || !username.equals(copy.getTag())) {
            response.put("message","警告：您所付罚款的书目不需要罚款");
            return response;
        }
        copy.setTempPrice();
        double price = copy.getTempPrice();
        try{
            // 成功，增加log信息，移除tag
            sendPayment(username, price);
            // 移除tag
            copy.setTag("");
            copyRepository.save(copy);
            // 增加log
            PayLog payLog = new PayLog(copy,price,payTime, generateCause(copy));
            payLogRepository.save(payLog);
            reader.addPayLog(payLog);
            readerRepository.save(reader);
            response.put("message","success");
        }catch (Exception e){
            response.put("message","警告：您的支付失败");
        }
        return response;
    }

    // 根据副本状态输出原因
    String generateCause(Copy copy){
        String result = "";
        switch (copy.getState()){
            case "available":
                result = "延期归还";
                break;
            case "damaged":
                result = "书籍损坏";
                break;
            case "lost":
                result = "书籍丢失";
                break;
        }
        return result;
    }


    // 向助教api发送请求
    void sendPayment(String username, double price) throws Exception{
        Map<String,String> result = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String,String> map = new HashMap<>();
        map.put("invoke_id", invoke_id);
        map.put("uid", username);
        map.put("amount", Double.toString(price));
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        restTemplate.postForEntity(paymentUrl,request,String.class);
    }

}
