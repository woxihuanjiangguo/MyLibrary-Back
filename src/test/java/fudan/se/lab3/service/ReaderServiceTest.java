package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.domain.people.Code;
import fudan.se.lab3.repository.CodeRepository;
import fudan.se.lab3.service.ReaderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest
class ReaderServiceTest {

    @Autowired
    private ReaderService readerService;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookService bookService;

    @Autowired
    private CodeRepository codeRepository;

    String token;
    String token_student;

    @Test
    void failedRegisterByDuplicatedUsername() {
        RegisterRequest request = new RegisterRequest("admin", "password", "", "1@q.com", "SuperLibrarian");
        assertEquals("Invalid format of your register info", readerService.register(request).get("message"));
    }

    @Test
    void failedRegisterByInvalidInfo() {
        RegisterRequest request = new RegisterRequest("usr111", "password", "", "1@q.com", "SuperLibrarian");
        Map<String, Object> result = readerService.register(request);
        String message = result.get("message").toString();
        assertEquals("Invalid format of your register info", message);
    }

    @Test
    void testpayFine() throws IOException, ParseException {
         MultipartFile multipartFile = new MultipartFile() { //初始化multipartfile
            @Override
            public String getName() {
                return null;
            }
            @Override
            public String getOriginalFilename() {
                return null;
            }
            @Override
            public String getContentType() {
                return null;
            }
            @Override
            public boolean isEmpty() {
                return false;
            }
            @Override
            public long getSize() {
                return 1;
            }
            @Override
            public byte[] getBytes(){
                return new byte[]{1};
            }
            @Override
            public InputStream getInputStream() {
                return null;
            }
            @Override
            public void transferTo(File file) throws IllegalStateException {
            }
        };
        Map<String , Object> map = authService.login(new LoginRequest("admin" , "password" , "邯郸" , "admin"));
        token = map.get("token").toString();//管理员的token
        bookService.PrototypeUpload(multipartFile, "name", "123-1-123-12345-1", "author", "intro", "time", "100");
        bookService.AddCopy(token , 3 , "123-1-123-12345-1");

        CodeRequest codeRequest = new CodeRequest("19302010024@fudan.edu.cn");
        readerService.code(codeRequest);
        String code = this.codeRepository.findByEmail("19302010024@fudan.edu.cn").getCode();
        RegisterRequest request = new RegisterRequest("19302010024", "password666", "19302010024@fudan.edu.cn", "Undergraduate", code);
        readerService.register(request);
        Map<String, Object> mapstudent = authService.login(new LoginRequest("19302010024", "password666", "邯郸", "user"));
        token_student = mapstudent.get("token").toString();//学生的token

        //让借和预约的书立刻过期
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");

        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-002"}  , token , new String[]{"available"});
        Map<String , Object> response1 = readerService.payFine(token_student ,"123-1-123-12345-1-001");
        Assertions.assertEquals(response1.get("message") , "警告：您所付罚款的书目不需要罚款");
        bookService.CheckOverdue();
        Map<String , Object> response2 = readerService.payFine(token_student ,"123-1-123-12345-1-002");
        Assertions.assertTrue(response2.get("message").equals("success") || response2.get("message").equals("警告：您的支付失败"));

    }

}
