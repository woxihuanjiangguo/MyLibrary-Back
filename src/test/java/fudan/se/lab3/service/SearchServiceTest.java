package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.domain.book.Comment;
import fudan.se.lab3.domain.book.Discussion;
import fudan.se.lab3.domain.people.Attributes;
import fudan.se.lab3.domain.people.Code;
import fudan.se.lab3.repository.CodeRepository;
import fudan.se.lab3.repository.CopyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Transactional
@SpringBootTest
public class SearchServiceTest {
    private final AuthService authService;
    private final BookService bookService;
    private final ReaderService readerService;
    private final CopyRepository copyRepository;
    private final CodeRepository codeRepository;
    private final SearchService searchService;

    @Autowired
    public SearchServiceTest(AuthService authService, BookService bookService, ReaderService readerService, CodeRepository codeRepository, CopyRepository copyRepository , SearchService searchService){
        this.authService = authService;
        this.bookService = bookService;
        this.readerService = readerService;
        this.copyRepository= copyRepository;
        this.codeRepository = codeRepository;
        this.searchService = searchService;
    }
    String token;
    String token_student;
    MultipartFile multipartFile;

    @BeforeEach
    public void before() throws IOException {
        Map<String , Object> map = authService.login(new LoginRequest("admin" , "password" , "邯郸" , "admin"));
        token = map.get("token").toString();//管理员的token

        try {
            // 先注册一个我的读者用户，会发出邮件但不用管
            codeRepository.save(new Code("666666" , "19302010024@fudan.edu.cn"));
            String code = this.codeRepository.findByEmail("19302010024@fudan.edu.cn").getCode();
            RegisterRequest request = new RegisterRequest("19302010024", "password666", "19302010024@fudan.edu.cn", "Undergraduate", code);
            readerService.register(request);
            Map<String, Object> mapstudent = authService.login(new LoginRequest("19302010024", "password666", "邯郸", "user"));
            token_student = mapstudent.get("token").toString();//学生的token
        }
        catch (Exception e){

        }

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
    void testCopySearchbyCid(){
        Assertions.assertEquals(searchService.CopySearchbyCid("123-1-123-12345-1-003").getPrototype().getBookName() , "name");
    }

    @Test
    void testCopySearchbyISBN(){
        Assertions.assertEquals(searchService.PrototypeSearchbyISBN("123-1-123-12345-1").getBookName() , "name");

    }

    @Test
    void CopySearchbyISBNCopy(){
        Assertions.assertEquals(searchService.CopySearchbyISBNCopy("123-1-123-12345-1").get(0).getPrototype().getBookName() , "name");

    }

    @Test
    void testCopySearchbyReserver(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001" , "123-1-123-12345-1-002"}  , token_student);
        Assertions.assertEquals(searchService.CopySearchbyReserver("19302010024").get(0).getReserverId() , "19302010024");
    }

    @Test
    void testPrototypeSearchbyAuthorlike(){
        Assertions.assertEquals(searchService.PrototypeSearchbyAuthorlike("auth").get(0).getBookName() , "name");
    }

    @Test
    void testPrototypeSearchbyBookNamelike(){
        Assertions.assertEquals(searchService.PrototypeSearchbyBookNamelike("nam").get(0).getBookName() , "name");

    }

    @Test
    void testCopySearchbyUser(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001" , "123-1-123-12345-1-002"}  , token_student);
        Assertions.assertEquals(searchService.CopySearchbyUser(token_student).size() , 2);
    }

    @Test
    void testcopySearchbyUsername(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001" , "123-1-123-12345-1-002"}  , token_student);
        Assertions.assertEquals(searchService.copySearchbyUsername("19302010024").size() , 2);
    }

    @Test
    void testCopySearchbyAdmin(){
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}  , "19302010024" , token);
        searchService.CopySearchbyAdmin(token);
        Assertions.assertEquals(searchService.CopySearchbyAdmin(token).size(), 1);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        Assertions.assertEquals(searchService.CopySearchbyAdmin(token).size(), 2);
    }

    @Test
    void testLogSearchbyReader(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001" , "123-1-123-12345-1-002"}  , token_student);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}  , "19302010024" , token);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        Assertions.assertEquals(searchService.LogSearchbyReader("19302010024").size() , 2);
    }

    @Test
    void testcopySearchAllbyOverdue(){
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001" , "123-1-123-12345-1-002"}  , token_student);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        Assertions.assertEquals(searchService.copySearchAllbyOverdue().size() , 3);
    }

    @Test
    void testcopySearchAllbyWhichOverdue(){
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001"}  , token_student);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        Assertions.assertEquals(searchService.copySearchAllbyWhichOverdue(1).size() , 1);
        Assertions.assertEquals(searchService.copySearchAllbyWhichOverdue(2).size() , 1);
    }

    @Test
    void testcopySearchbyOverdue() throws ParseException {
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001", "123-1-123-12345-1-002"}, token_student);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}, "19302010024", token);
        Assertions.assertEquals(searchService.copySearchAllbyOverdue().size(), 3);
    }

    @Test
    void testcopySearchbyTagAll() throws ParseException {
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
            bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
            bookService.CopyReturn(new String[]{"123-1-123-12345-1-002"} , token , new String[]{"available"} );
            Assertions.assertEquals(searchService.copySearchbyTagAll().size() , 1);
        }

    @Test
    void testcopySearchbyTag() throws ParseException {
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-002"} , token , new String[]{"available"} );
        Assertions.assertEquals(searchService.copySearchbyTag(token_student).size() , 1);
    }

    @Test
    void testcopySearchbyTagUsername() throws ParseException {
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-002"} , token , new String[]{"available"} );
        Assertions.assertEquals(searchService.copySearchbyTagUsername("19302010024").size() , 1);
    }


    @Test
    void testPrototypeSearchDefaultAll(){
        Assertions.assertEquals(searchService.PrototypeSearchDefaultAll("default").size() , 1);
    }

    @Test
    void testadminLogs(){
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}  , "19302010024" , token);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        Assertions.assertEquals(searchService.adminLogs(token).size() , 2);
    }

    @Test
    void testreaderPayLogs(){
        Assertions.assertEquals(searchService.readerPayLogs("19302010024").size() , 0);
    }

    @Test
    void testgetAttributes(){
        Map<String, Object> result = searchService.getAttributes();
        Attributes attributes = (Attributes)result.get("attr1");
        Assertions.assertEquals(attributes.getMaxBorrow() , 10);
    }

    @Test
    void testsearchOneReader(){
        Assertions.assertEquals(searchService.searchOneReader("19302010024").get(0).getMaxBorrow() , 5);
    }

    @Test
    void testsearchsearchOneTypeUser(){
        Assertions.assertEquals(searchService.searchOneTypeUser("Undergraduate").get(0).getMaxBorrow() , 5);
        Assertions.assertEquals(searchService.searchOneTypeUser("Postgraduate").size() , 0);
    }

    @Test
    void testsearchAllUsers(){
        Assertions.assertEquals(searchService.searchAllUsers().size() , 1);
        }



    //new
    @Test
    void testsearchOneReaderComment() throws ParseException {
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"}, token, new String[]{"available"});
        bookService.comment(1,"test of searchService","123-1-123-12345-1",token_student);
        List<Comment> temp = searchService.searchOneReaderComment("19302010024");
        Assertions.assertEquals(1, temp.size());
        Assertions.assertEquals("test of searchService", temp.get(temp.size()-1).getContent());
    }

}
