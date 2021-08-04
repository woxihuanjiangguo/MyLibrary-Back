package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.domain.book.Copy;
import fudan.se.lab3.domain.people.Code;
import fudan.se.lab3.domain.people.Reader;
import fudan.se.lab3.repository.CodeRepository;
import fudan.se.lab3.repository.CopyRepository;
import fudan.se.lab3.repository.PrototypeRepository;
import fudan.se.lab3.repository.ReaderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.method.P;
import org.springframework.test.context.TestExecutionListeners;
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
class BookServiceTest {
    private final AuthService authService;
    private final BookService bookService;
    private final ReaderService readerService;
    private final PrototypeRepository prototypeRepository;
    private final CopyRepository copyRepository;
    private final CodeRepository codeRepository;
    private final ReaderRepository readerRepository;

    @Autowired
    public BookServiceTest(AuthService authService, ReaderRepository readerRepository, BookService bookService, ReaderService readerService, PrototypeRepository prototypeRepository ,CodeRepository codeRepository, CopyRepository copyRepository){
        this.authService = authService;
        this.bookService = bookService;
        this.readerRepository = readerRepository;
        this.readerService = readerService;
        this.prototypeRepository = prototypeRepository;
        this.copyRepository= copyRepository;
        this.codeRepository = codeRepository;
    }
    String token;
    String token_student;
    MultipartFile multipartFile;

    //测试默认仓库内没有书本，均为新加入书本

    @BeforeEach
    public void before() throws IOException {
        Map<String , Object> map = authService.login(new LoginRequest("admin" , "password" , "邯郸" , "admin"));
        token = map.get("token").toString();//管理员的token

        // 先注册一个我的读者用户，会发出邮件但不用管
        codeRepository.save(new Code("666666" , "19302010024@fudan.edu.cn"));
        String code = this.codeRepository.findByEmail("19302010024@fudan.edu.cn").getCode();
        RegisterRequest request = new RegisterRequest("19302010024", "password666", "19302010024@fudan.edu.cn", "Undergraduate", code);
        readerService.register(request);
        Map<String , Object> mapstudent = authService.login(new LoginRequest("19302010024" , "password666" , "邯郸" , "user"));
        token_student = mapstudent.get("token").toString();//学生的token

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

    //根据cid修改副本状态 available,reserved,borrowed,damaged,lost,返回修改后的副本
    private void CopySetState(String cid , String state){
        Copy copy = copyRepository.findByCid(cid);
        copy.setState(state);
        copyRepository.save(copy);
    }

    //根据cid修改副本位置，邯郸，枫林，江湾，张江，返回修改后的副本
    private void CopySetregion(String cid , String region){
        Copy copy = copyRepository.findByCid(cid);
        copy.setRegion(region);
        copyRepository.save(copy);
    }

    @Test
    void normalUpload() throws IOException {
        Assertions.assertTrue(bookService.PrototypeUpload(multipartFile, "name1", "123-1-123-12345-2", "author1", "intro1", "time", "100"));
    }

    @Test
    void duplicatedUpload() throws IOException{
        normalUpload();
        Assertions.assertFalse(bookService.PrototypeUpload(multipartFile, "name1", "123-1-123-12345-2", "author1", "intro1", "time", "100"));
    }

    @Test
    void normalAddNewCopy() throws IOException {
        bookService.PrototypeUpload(multipartFile, "name1", "123-1-123-12345-2", "author1", "intro1", "time", "100");
        bookService.AddCopy(token , 2 , "123-1-123-12345-2");
        Assertions.assertEquals(copyRepository.findByPrototype(prototypeRepository.findByISBN("123-1-123-12345-2")).size() , 2);
    }

    @Test
    void normalAddExistCopy(){
        bookService.AddCopy(token , 2 , "123-1-123-12345-1");
        Assertions.assertEquals(copyRepository.findByPrototype(prototypeRepository.findByISBN("123-1-123-12345-1")).size() , 5);
    }

    @Test
    void nonePrototypeAddCopy(){
        Assertions.assertEquals(bookService.AddCopy(token , 1 , "123-1-123-12345-2") , 1);
    }

    @Test
    void wrongBookNumAddCopy() {
        Assertions.assertEquals(bookService.AddCopy(token , 0 , "123-1-123-12345-1") , 2);
        Assertions.assertEquals(bookService.AddCopy(token , -1 , "123-1-123-12345-1") , 2);
    }

    @Test
    void largeNumberAddCopy() {
        Assertions.assertEquals(bookService.AddCopy(token , 200 , "123-1-123-12345-1") , 0);
    }

    @Test
    void normalBookBorrow(){
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001","123-1-123-12345-1-002","123-1-123-12345-1-003"}, "19302010024" , token);
        Assertions.assertEquals(copyRepository.findByCid("123-1-123-12345-1-001").getState() , "borrowed");
    }

    @Test
    void wrongRegionBookBorrow(){
        CopySetregion("123-1-123-12345-1-001" , "江湾");
        List<Copy> copyList = (List<Copy>)bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024", token).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void wrongStateBookBorrow() {
        CopySetState("123-1-123-12345-1-001" , "borrowed");
        List<Copy> copyList = (List<Copy>)bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024", token).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void normalBookGetReserved(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001","123-1-123-12345-1-002","123-1-123-12345-1-003"} , token_student);
        bookService.CopyGetReserved(new String[]{"123-1-123-12345-1-001","123-1-123-12345-1-002","123-1-123-12345-1-003"}, "19302010024" , token);
        Assertions.assertEquals(copyRepository.findByCid("123-1-123-12345-1-001").getState() , "borrowed");
    }

    @Test
    void wrongRegionBookGetReserved(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001"} , token_student);
        CopySetregion("123-1-123-12345-1-001" , "江湾");
        List<Copy> copyList = (List<Copy>)bookService.CopyGetReserved(new String[]{"123-1-123-12345-1-001"}, "19302010024", token).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void wrongReserverBookGetReserved(){
        List<Copy> copyList = (List<Copy>)bookService.CopyGetReserved(new String[]{"123-1-123-12345-1-001"} ,"19302010024" , token).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void wrongStateBookGetReserved() {
        List<Copy> copyList = (List<Copy>)bookService.CopyGetReserved(new String[]{"123-1-123-12345-1-001"}, "19302010024", token).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void normalBookReturn() throws ParseException {
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024" , token);
        Assertions.assertTrue(bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"}, token, new String[]{"available"}));
    }

    @Test
    void wrongStateBookReturn() throws  ParseException {
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001"} , token_student);
        bookService.CopyGetReserved(new String[]{"123-1-123-12345-1-001"}, "19302010024" , token);
        CopySetState("123-1-123-12345-1-001" , "available");
        Assertions.assertFalse(bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"}, token, new String[]{"available"}));
    }

    @Test
    void normalBookReserve(){
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001","123-1-123-12345-1-002"} , token_student);
        Assertions.assertEquals(copyRepository.findByCid("123-1-123-12345-1-001").getState() , "reserved");
    }

    @Test
    void wrongStateBookReserve() {
        CopySetState("123-1-123-12345-1-001" , "borrowed");
        List<Copy> copyList = (List<Copy>)bookService.CopyReserve(new String[]{"123-1-123-12345-1-001"}, token_student).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void testCheckOverdue() throws ParseException {
        authService.alterAttributes("Undergraduate" , 5 , "0,0,0,0" , "0,0,0,0");
        bookService.CopyReserve(new String[]{"123-1-123-12345-1-001"}  , token_student);
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-002"}  , "19302010024" , token);
        bookService.CheckOverdue();

    }


    //new test

    //test of add part

    @Test
    void lowScoreBookReserve(){
        Reader temp = readerRepository.findByUsername("19302010024");
        temp.setScore(-1);
        readerRepository.save(temp);

        List<Copy> copyList = (List<Copy>)bookService.CopyReserve(new String[]{"123-1-123-12345-1-002"}, token_student).get("copyList");
        Assertions.assertEquals(0 , copyList.size());
    }

    @Test
    void normalComment() throws ParseException {
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"}, token, new String[]{"available"});
        Assertions.assertEquals(true,bookService.comment(1,"is good","123-1-123-12345-1",token_student));
    }
    @Test
    void severalComment(){// 多次评论
        bookService.comment(1,"is good","123-1-123-12345-1",token_student);
        Assertions.assertEquals(false,bookService.comment(1,"is very good","123-1-123-12345-1",token_student));
    }
    @Test
    void bookStateDamageComment() throws ParseException {
        bookService.CopyBorrow(new String[]{"123-1-123-12345-1-001"}, "19302010024" , token);
        bookService.CopyReturn(new String[]{"123-1-123-12345-1-001"},token,new String[]{"damaged"});
        Assertions.assertEquals(false,bookService.comment(1,"is very good","123-1-123-12345-1",token_student));
    }
    @Test
    void normalDiscussion(){
       // Assertions.assertEquals(true,bookService.discussion("star","is good",token_student));
    }
    @Test
    void failedDiscussion(){
        //Assertions.assertEquals(false,bookService.discussion("","",token_student));
    }

}
