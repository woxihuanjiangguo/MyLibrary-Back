package fudan.se.lab3.service;

import fudan.se.lab3.common.Utils;
import fudan.se.lab3.domain.book.*;
import fudan.se.lab3.domain.people.Reader;
import fudan.se.lab3.repository.*;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class BookService {
    private final CopyRepository copyRepository;
    private final PrototypeRepository prototypeRepository;
    private final ReaderRepository readerRepository;
    private final LogRepository logRepository;
    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final JavaMailSender javaMailSender;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public BookService(CopyRepository copyRepository, PrototypeRepository prototypeRepository, ReaderRepository readerRepository, LogRepository logRepository, CommentRepository commentRepository, DiscussionRepository discussionRepository, JwtTokenUtil jwtTokenUtil, JavaMailSender javaMailSender) {
        this.copyRepository = copyRepository;
        this.prototypeRepository = prototypeRepository;
        this.readerRepository = readerRepository;
        this.logRepository = logRepository;
        this.commentRepository = commentRepository;
        this.discussionRepository = discussionRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.javaMailSender = javaMailSender;
    }

    private static final String SendFrom = "woxihuanruanjian@163.com";


    //新增书本
    public boolean PrototypeUpload(MultipartFile multipartFile, String bookName, String ISBN, String author, String intro, String publishTime, String price) throws IOException {
        if (prototypeRepository.findByISBN(ISBN) != null) { //同种书本不能存在两种
            return false;
        }
        PicFile picFile = new PicFile(multipartFile.getBytes());
        Prototype prototype = new Prototype(ISBN, bookName, author, intro, publishTime, Double.parseDouble(price), picFile);
        prototypeRepository.save(prototype);
        return true;
    }


    //新增副本 状态为available
    public int AddCopy(String token, int bookNum, String ISBN) {
        Prototype prototype = prototypeRepository.findByISBN(ISBN);
        List<Copy> copyList = copyRepository.findByPrototype(prototypeRepository.findByISBN(ISBN));
        if (prototype == null) {
            return 1;
        }
        if (bookNum <= 0) {
            return 2;
        }
        for (int i = 1; i <= bookNum; i++) {
            Copy copy = new Copy(prototype, jwtTokenUtil.getRegionFromToken(token), "available", jwtTokenUtil.getUsernameFromToken(token));
            int number = copyList.size() + i;
            if (number < 10) {
                copy.setCid(ISBN + "-00" + number);
            } else if (number < 100) {
                copy.setCid(ISBN + "-0" + number);
            } else {
                copy.setCid(ISBN + "-" + number);
            }
            copyRepository.save(copy);
        }
        return 0;
    }

    public Map<String , Object> CopyBorrow(String[] cid, String id, String token) {
        Map<String , Object> result = new HashMap<>();
        Reader reader = readerRepository.findByUsername(id);
        for (String s : cid) { //判断管理员地址与书本是否相同、书本状态是否为available
            Copy copy = copyRepository.findByCid(s);
            if (!copy.getRegion().equals(jwtTokenUtil.getRegionFromToken(token))) {
                result.put("message" , "副本地区与您的地址不一致");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
            else if(!copy.getState().equals("available")){
                result.put("message" , "副本状态不为可用，无法借阅");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
        }
        return getCopy(cid, id, token, true);
    }

    public Map<String , Object> CopyGetReserved(String[] cid, String id, String token) {
        Map<String , Object> result = new HashMap<>();
        for (String s : cid) { //判断管理员地址与书本是否相同、书本状态是否为reserved
            Copy copy = copyRepository.findByCid(s);
            if (!copy.getRegion().equals(jwtTokenUtil.getRegionFromToken(token))) {
                result.put("message" , "副本地区与您的地址不一致");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
            else if(!copy.getState().equals("reserved")){
                result.put("message" , "副本状态为不为被预约，无法借阅");
                result.put("copyList" , new ArrayList<Copy>());
                return result;

            }
            else if (!id.equals(copy.getReserverId())) {
                result.put("message" , "预约人与借书人id不一致");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
        }
        return getCopy(cid, id, token, false);
    }

    private Map<String , Object> getCopy(String[] cid, String id, String token, boolean isBorrow) {
        Map<String , Object> result = new HashMap<>();
        List<Copy> copyList = new ArrayList<>();
        Date today = new Date();
        Reader reader = readerRepository.findByUsername(id);
        for (String s : cid) {
            if ((isBorrow && (reader.getCurrentNumber() == reader.getMaxBorrow()))) {
                result.put("message" , "已到达最大借阅数量");
                break;
            }
            else if(reader.getScore() < 0){
                result.put("message" , "信用分不足，无法借书");
                break;
            }
            else{
                result.put("message" , "success");
            }
            Copy copy = copyRepository.findByCid(s);
            boolean flag = false;   // true 则 log仓库为空
            Log log = new Log();

            if (copy.getLogList().size() == 0) {     //获取最新的log
                flag = true;
            } else {
                log = copy.getLogList().get(copy.getLogList().size() - 1);
            }

            // 处理log信息
            if (!flag && log.getReservedTime() != null && log.getBorrowedTime() == null) {//有预约，无借阅
                log.setBorrowedTime(format.format(today));
                log.setBorrowAdmin(jwtTokenUtil.getUsernameFromToken(token));
                log.setBorrowerId(id);
                log.setBorrowRegion(copy.getRegion());
                logRepository.saveAndFlush(log);
            } else {
                //无预约直接借阅，此时新加入log
                Log newlog = new Log(copy);
                newlog.setBorrowedTime(format.format(today));
                newlog.setBorrowAdmin(jwtTokenUtil.getUsernameFromToken(token));
                newlog.setBorrowRegion(copy.getRegion());
                newlog.setBorrowerId(id);
                logRepository.saveAndFlush(newlog);
                copy.getLogList().add(newlog);
            }
            copy.setRegion(null);
            copy.setBorrowerId(id);
            copy.setReserverId(null);
            copy.setExpectedBorrowTime(null);

            String[] temp = reader.getBorrowDuration().split(",");
            getDuration(temp);

            copy.setExpectedReturnTime(format.format(new Date(today.getTime() + getDuration(temp))));

            copy.setState("borrowed");
            copyRepository.saveAndFlush(copy);
            copyList.add(copy);
            if (isBorrow) {
                reader.addCurrentNumber();
                readerRepository.saveAndFlush(reader);
            }
        }
        result.put("copyList" , copyList);
        return result;
    }

    private long getDuration(String[] temp) {
        int[] duration = new int[4];
        for(int i = 0 ; i < 4 ; i++){
            duration[i] = Integer.parseInt(temp[i]);
        }

        return  (long) duration[0] * 24 * 3600 * 1000 + (long) duration[1] * 3600 * 1000 + (long) duration[2] * 60 * 100 + duration[3] * 1000L;
    }

    public boolean CopyReturn(String[] cid, String token, String[] state) throws ParseException {
        for (String s : cid) {
            if (!copyRepository.findByCid(s).getState().equals("borrowed")) {
                return false;
            }
        }
        Date today = new Date();
        for (int i = 0; i < cid.length; i++) {
            Copy copy = copyRepository.findByCid(cid[i]);
            Reader reader = readerRepository.findByUsername(copy.getBorrowerId());

            Log log = copy.getLogList().get(copy.getLogList().size() - 1);
            log.setReturnTime(format.format(today));
            log.setReturnAdmin(jwtTokenUtil.getUsernameFromToken(token));
            log.setReturnRegion(jwtTokenUtil.getRegionFromToken(token));
            logRepository.saveAndFlush(log);
            copy.setRegion(jwtTokenUtil.getRegionFromToken(token));
            Date shouldreturn = format.parse(copy.getExpectedReturnTime());

            if ("lost".equals(state[i])) {
                copy.setTag(copy.getBorrowerId());
                reader.decrementScore(40);
                sendBadCreditEmail(0 , reader.getUsername() , copy);
            }
            else if("damaged".equals(state[i])){
                copy.setTag(copy.getBorrowerId());
                reader.decrementScore(30);
                sendBadCreditEmail(1 , reader.getUsername() , copy);
            }
            else if(today.compareTo(shouldreturn) > 0){
                copy.setTag(copy.getBorrowerId());
                reader.decrementScore(20);
                sendBadCreditEmail(2 , reader.getUsername() , copy);
            }
            else{
                reader.addNotCommentedList(prototypeRepository.findByISBN(copy.getPrototype().getISBN()));
            }

            copy.setExpectedReturnTime(null);
            copy.setBorrowerId(null);

            copy.setState(state[i]);
            copyRepository.saveAndFlush(copy);
            reader.decrementCurrentNumber();
            readerRepository.saveAndFlush(reader);
        }
        return true;
    }

    public Map<String , Object> CopyReserve(String[] cid, String token) {
        List<Copy> copyList = new ArrayList<>();
        Date today = new Date();
        String id = jwtTokenUtil.getUsernameFromToken(token);
        Map<String, Object> result = new HashMap<>();
        Reader reader = readerRepository.findByUsername(id);
        for (String s : cid) {
            if (!copyRepository.findByCid(s).getState().equals("available")) {
                result.put("message" , "副本状态不为可用，无法预约");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
            else if(reader.getScore() < 50){
                result.put("message" , "信用分不足，无法预约");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
            else if(reader.getCurrentNumber() == reader.getMaxBorrow()){
                result.put("message" , "已达最大借阅数量");
                result.put("copyList" , new ArrayList<Copy>());
                return result;
            }
            else{
                result.put("message" , "success");
            }
        }
        for (String s : cid) {
            reader.addCurrentNumber();
            Copy copy = copyRepository.findByCid(s);
            Log newlog = new Log(copy);
            newlog.setReservedTime(format.format(today));
            newlog.setReserverId(id);
            logRepository.saveAndFlush(newlog);
            copy.getLogList().add(newlog);

            String[] temp = reader.getReserveDuration().split(",");

            copy.setExpectedBorrowTime(format.format(new Date(today.getTime() + getDuration(temp))));
            copy.setReserverId(id);
            copy.setState("reserved");
            copyRepository.saveAndFlush(copy);
            readerRepository.saveAndFlush(reader);
            copyList.add(copy);
        }

        result.put("copyList" , copyList);
        return result;
    }

    public boolean comment(int star , String content , String ISBN , String token){
        String id = jwtTokenUtil.getUsernameFromToken(token);
        Reader reader = readerRepository.findByUsername(id);
        Prototype prototype = prototypeRepository.findByISBN(ISBN);

        for(Comment comment : prototype.getCommentList()){ //只能评论一次
            if(comment.getReader().equals(reader)){
                return false;
            }
        }

        if(!reader.getNotCommentedPrototypeList().contains(prototype) || star < 0 || star > 5){ //损坏丢失不能评论，分数0-5
            return false;
        }

        reader.getNotCommentedPrototypeList().remove(prototype);
        readerRepository.save(reader);
        Comment comment = new Comment(star , reader , new Date() , ISBN , content);
        commentRepository.save(comment);
        prototype.getCommentList().add(comment);
        prototype.countStar(star);
        prototypeRepository.save(prototype);
        return true;
    }

    public boolean discussion(String content , String reply , String token , Long commentId){
        Comment comment = commentRepository.findCommentById(commentId);
        Reader reader = readerRepository.findByUsername(jwtTokenUtil.getUsernameFromToken(token));
        Discussion discussion = new Discussion(new Date() , reader , reply , content, comment);
        discussionRepository.save(discussion);
        comment.getDiscussionList().add(discussion);
        commentRepository.save(comment);
        remindDiscussionEmail(reader.getUsername() , content , comment);
        return true;
    }


    /************************************以下为控制发提醒邮件的部分*********************************************/
    public void CheckOverdue() {
        List<Copy> copyList = copyRepository.findAll();
        Map<String, String> map = new HashMap<>();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("您的 MyLibrary 有提示消息");
        message.setFrom(SendFrom);
        message.setSentDate(new Date());

        for (Copy copy : copyList) {
            generateBadCopyMsg(map, copy);
        }
        for (String reader : map.keySet()) {
            try {
                message.setText(map.get(reader));
                message.setTo(reader + "@fudan.edu.cn");
                javaMailSender.send(message);
            } catch (Exception ignored) {
            }
        }
    }

    private void generateBadCopyMsg(Map<String, String> map, Copy copy){
        Date today = new Date();
        boolean flag1 = false, flag2 = false;
        String expectedBorrowTime = copy.getExpectedBorrowTime();
        String expectedReturnTime = copy.getExpectedReturnTime();
        try {
            if (Utils.isStrNotNull(expectedBorrowTime)) {
                Date expectedBorrowDate = format.parse(expectedBorrowTime);
                flag1 = today.compareTo(expectedBorrowDate) >= 0;
            }
            if(Utils.isStrNotNull(expectedReturnTime)){
                Date expectedReturnDate = format.parse(expectedReturnTime);
                flag2 = today.compareTo(expectedReturnDate) >= 0;
            }
        }catch (Exception e){
            return;
        }
        String tag = copy.getTag();
        if (flag1) {
            Reader reader = readerRepository.findByUsername(copy.getReserverId());
            reader.decrementScore(10);
            readerRepository.save(reader);
            String msg = "您有已预约的书到期， 副本编号: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n";
            msg += "已扣除您的信用分10分\n";
            map.put(copy.getReserverId(),
                    map.containsKey(copy.getReserverId()) ? map.get(copy.getReserverId()) + msg : msg);
            copy.setState("available");
            copy.setReserverId(null);
            copy.setExpectedBorrowTime(null);
            copyRepository.save(copy);
        }
        if (flag2) {
            String msg = "您有已借阅的书到期， 副本编号: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n";
            map.put(copy.getBorrowerId(),
                    map.containsKey(copy.getBorrowerId()) ? map.get(copy.getBorrowerId()) + msg : msg);
        }
        if (Utils.isStrNotNull(tag)) {
            String msg = "您有未付罚款的书本条目， 副本编号: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n";
            map.put(copy.getTag(),
                    map.containsKey(copy.getTag()) ? map.get(copy.getTag()) + msg : msg);
        }
    }

    private void sendBadCreditEmail(int type , String id , Copy copy){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("您的 MyLibrary 有提示消息");
        message.setFrom(SendFrom);
        message.setSentDate(new Date());

        try {
            switch (type){
                case 0 : message.setText("您借阅的书本已遗失: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n" + "已扣除您的信用分40分\n");break;
                case 1 : message.setText("您借阅的书本已损坏: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n" + "已扣除您的信用分30分\n");break;
                case 2 : message.setText("您借阅的书本已过期: " + copy.getCid() + "， 书名： " + copy.getPrototype().getBookName() + "\n" + "已扣除您的信用分20分\n");break;
            }

            message.setTo(id + "@fudan.edu.cn");
            javaMailSender.send(message);
        }
        catch (Exception ignored){}

    }

    private void remindDiscussionEmail(String id , String content , Comment comment){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("您的 MyLibrary 有提示消息");
        message.setFrom(SendFrom);
        message.setSentDate(new Date());
        try {
            message.setText("您的评论：" + comment.getContent() + "有新的回复：\n" + content);
            message.setTo(id + "@fudan.edu.cn");
            javaMailSender.send(message);
        }
        catch (Exception ignored){}
    }



}
