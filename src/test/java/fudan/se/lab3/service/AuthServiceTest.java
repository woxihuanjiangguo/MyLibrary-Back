package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.AddRequest;
import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.domain.book.Comment;
import fudan.se.lab3.domain.book.Discussion;
import fudan.se.lab3.domain.people.Code;
import fudan.se.lab3.exception.BadAuthRequestException;
import fudan.se.lab3.exception.PasswordIncorrectException;
import fudan.se.lab3.repository.CodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ReaderService readerService;

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private Map<String, Object> map;
    private String token;
    private String token_student;
    MultipartFile multipartFile;

    @BeforeEach
    public void before() throws IOException {
        Map<String, Object> map = authService.login(new LoginRequest("admin", "password", "邯郸", "admin"));
        token = map.get("token").toString();

        // 先注册一个我的读者用户，会发出邮件但不用管,这边假设已经注册好了
        codeRepository.save(new Code("666666" , "19302010007@fudan.edu.cn"));
        String code = this.codeRepository.findByEmail("19302010007@fudan.edu.cn").getCode();
        RegisterRequest request = new RegisterRequest("19302010007", "password666", "19302010007@fudan.edu.cn", "Teacher", code);
        readerService.register(request);
        Map<String,Object> mapOfStudent = authService.login(new LoginRequest("19302010007","password666","邯郸","user"));
        token_student = mapOfStudent.get("token").toString();

        multipartFile = new MultipartFile() { //初始化multipartfile
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
        //上传一个原型并增加三个副本
        bookService.PrototypeUpload(multipartFile, "name", "123-1-123-12345-1", "author", "intro", "time", "100");
        bookService.AddCopy(token , 3 , "123-1-123-12345-1");
    }

    @Test
    void successfullyLogin() {
        LoginRequest request = new LoginRequest("admin", "password", "张江", "admin");
        authService.login(request);
    }

    @Test
    void FailedLoginByNoSuchUser() {
        LoginRequest request = new LoginRequest("usr1", "password", "张江", "user");
        try {
            authService.login(request);
        } catch (UsernameNotFoundException ex) {
            assertEquals("usr1", ex.getMessage());
        }
    }

    @Test
    void FailedLoginByPasswordIncorrect() {
        LoginRequest request = new LoginRequest("admin", "wrong", "邯郸", "user");
        try {
            authService.login(request);
        } catch (PasswordIncorrectException ex) {
            assertEquals("Username 'admin' password incorrect", ex.getMessage());
        }
    }

    @Test
    void successfulUserLogin() {
        LoginRequest request2 = new LoginRequest("19302010007", "password666", "邯郸", "user");
        authService.login(request2);
    }

    @Test
    void successfulLibrarianLogin() {
        //type user/admin
        AddRequest request = new AddRequest("testAdmin01", "password666");
        authService.add(request);
        LoginRequest request2 = new LoginRequest("testAdmin01", "password666", "邯郸", "admin");
        authService.login(request2);
    }

    @Test
    void failedUserLogin() {
        LoginRequest request2 = new LoginRequest("19302010007", "password666", "邯郸", "admin");
        try {
            authService.login(request2);
        } catch (BadAuthRequestException ex) {
            assertEquals("Bad auth type requested or bad location selected", ex.getMessage());
        }
    }

    @Test
    void failedLibrarianLogin() {
        //type user/admin
        AddRequest request = new AddRequest("19302010007", "password666");
        authService.add(request);
        LoginRequest request2 = new LoginRequest("19302010007", "password666", "邯2郸", "admin");
        try {
            authService.login(request2);
        } catch (BadAuthRequestException ex) {
            assertEquals("Bad auth type requested or bad location selected", ex.getMessage());
        }

    }

    @Test
    void testAddExistingAdmin() {
        AddRequest request = new AddRequest("admin", "qwe123");
        assertEquals("Duplicated username not allowed", authService.add(request).get("message"));
    }

    @Test
    void testIllegalAddAdmin() {
        AddRequest request = new AddRequest("11", "1");
        assertEquals("Illegal username or password", authService.add(request).get("message"));
    }

    @Test
    void testFailedChangeByOriginalDifference() {
        assertEquals("Your first input must match your original password", authService.changePassword("password66", "123456qwe", token).get("message"));
    }

    @Test
    void FailedAddAdminByIllegalNewPassword() {
        assertEquals("Illegal password, 6-32 digits or letters", authService.changePassword("password", "12345", token).get("message"));
    }

    @Test
    void FailedAddTeacherOrStudentByIllegalNewPassword() {
        LoginRequest request1 = new LoginRequest("19302010007", "password666", "邯郸", "user");
        Map<String, Object> map = authService.login(request1);
        String token1 = map.get("token").toString();
        authService.login(request1);
        assertEquals("Illegal password, 6-32 digits, letters - or _, excluding username, containing two kinds at least", authService.changePassword("password666", "12345", token1).get("message"));
    }

    @Test
    void successfullyChangePassword() {
        authService.changePassword("password", "123456", token);
    }





    //new test
    @Test
    void successfullyResetScore(){
        Map<String,Object> temp = authService.resetScore("19302010007");
        assertEquals("success",temp.get("message"));
    }
    @Test
    void failedResetScore(){// 对一个不存在的用户重置分数
        Map<String,Object> temp = authService.resetScore("19302010099");
        assertEquals("用户不存在！",temp.get("message"));
    }

    @Test
    void successfullyDeleteBadComment() throws ParseException {
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010007" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"}, token, new String[]{"available"});
        bookService.comment(1,"is bad","123-1-123-12345-1",token_student);
        List<Comment> temp = searchService.searchOneReaderComment("19302010007");
        Long test = temp.get(temp.size()-1).getId();
        assertEquals("success",authService.deleteBadComment(test).get("message"));
    }
    @Test
    void failedDeleteBadComment(){
        Long test = 10000000L;
        assertEquals("评论不存在！",authService.deleteBadComment(test).get("message"));
    }

    /*@Test
    void successfullyDeleteBadDiscussion(){
        // 先增加一条评论
        //TODO
        //bookService.discussion("is bad","",token_student);
        List<Discussion> temp = searchService.searchOneReaderDiscussion("19302010007");
        Long test = temp.get(temp.size()-1).getId();
        assertEquals("success",authService.deleteBadComment(test).get("message"));
    }*/
    @Test
    void failedDeleteBadDiscussion(){
        Long test = 10000000L;
        assertEquals("评论不存在！",authService.deleteBadComment(test).get("message"));
    }
}
