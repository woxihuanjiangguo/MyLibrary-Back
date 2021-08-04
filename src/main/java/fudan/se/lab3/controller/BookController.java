package fudan.se.lab3.controller;

import fudan.se.lab3.domain.book.*;
import fudan.se.lab3.repository.CopyRepository;
import fudan.se.lab3.repository.PrototypeRepository;
import fudan.se.lab3.repository.UserRepository;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import fudan.se.lab3.service.BookService;
import fudan.se.lab3.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BookController {

    private final BookService bookService;

    private final PrototypeRepository prototypeRepository;

    private final CopyRepository copyRepository;

    private final UserRepository userRepository;

    private final SearchService searchService;

    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public BookController(BookService bookService, PrototypeRepository prototypeRepository, CopyRepository copyRepository, UserRepository userRepository, JwtTokenUtil jwtTokenUtil, SearchService searchService) {
        this.bookService = bookService;
        this.prototypeRepository = prototypeRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.searchService = searchService;
    }

    @GetMapping(value = "/welcome2")
    public String Test() {
        return "Welcome!!!!!!!!!--version2";
    }

    // 全部书本原型
    @PostMapping(value = "/books/search/all")
    public ResponseEntity<?> prototypeSearchDefaultAll(@RequestParam("number") String number) {
        Map<String, Object> result = new HashMap<>();
        result.put("prototypeList", searchService.PrototypeSearchDefaultAll(number));
        return ResponseEntity.ok(result);
    }

    // 借阅、归还、取预约的时候用到
    @PostMapping(value = "/books/search/cid")
    public ResponseEntity<?> copySearchbyCid(@RequestParam("cid") String cid) {//根据cid查找
        Map<String, Object> result = new HashMap<>();
        result.put("copy", searchService.CopySearchbyCid(cid));
        return ResponseEntity.ok(result);
    }

    // 查询ISBN 以及 ISBNCopy
    @PostMapping(value = "/books/search/ISBN")
    public ResponseEntity<?> prototypeSearchbyISBN(@RequestParam("ISBN") String ISBN) {
        Map<String, Object> result = new HashMap<>();
        result.put("prototypeList", searchService.PrototypeSearchbyISBN(ISBN));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/books/search/ISBNCopy")
    public ResponseEntity<?> copySearchbyISBNCopy(@RequestParam("ISBN") String ISBN) {
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", searchService.CopySearchbyISBNCopy(ISBN));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/books/search/reserver")
    public ResponseEntity<?> copySearchbyReserver(@RequestParam("reserver") String reserver) {
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", searchService.CopySearchbyReserver(reserver));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/books/search/authorlike")//改为每本只返回一本
    public ResponseEntity<?> prototypeSearchbyAuthorlike(@RequestParam("author") String author) {
        Map<String, Object> result = new HashMap<>();
        result.put("prototypeList", searchService.PrototypeSearchbyAuthorlike(author));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/books/search/bookNamelike")//改为每本只返回一本
    public ResponseEntity<?> prototypeSearchbyBookNamelike(@RequestParam("bookName") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("prototypeList", searchService.PrototypeSearchbyBookNamelike(name));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/books/search/user")
    public ResponseEntity<?> copySearchbyUser(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", searchService.CopySearchbyUser(token));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/system/search/admin")
    public ResponseEntity<?> copySearchbyAdmin(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", searchService.CopySearchbyAdmin(token));
        return ResponseEntity.ok(result);
    }

    // 超管的全部搜索 ---- 所有有问题的书：没付钱  或者是 超期
    @PostMapping("/system/search/allBadThings")
    public ResponseEntity<?> copySearchAllbyProblems(){
        Map<String, Object> result = new HashMap<>();
        result.put("copyListBorrowOverdue", searchService.copySearchAllbyWhichOverdue(1));
        result.put("copyListReserveOverdue", searchService.copySearchAllbyWhichOverdue(2));
        result.put("copyListUnpaid", searchService.copySearchbyTagAll());
        return ResponseEntity.ok(result);
    }

    //读者的个人搜索 1 没付钱
    @PostMapping(value = "/reader/search/unpaid")
    public ResponseEntity<?> copySearchbyTag(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", searchService.copySearchbyTag(token));
        return ResponseEntity.ok(result);
    }


    /************************************查询部分结束*************************************/
    /************************************书本操作逻辑开启*************************************/

    @PostMapping(value = "/system/upload")//新增书籍为原型，不是具体书本
    public ResponseEntity<?> prototypeUpload(@RequestParam("fileImg") MultipartFile multipartFile,
                                             @RequestParam("bookName") String bookName,
                                             @RequestParam("ISBN") String ISBN,
                                             @RequestParam("author") String author,
                                             @RequestParam("intro") String intro,
                                             @RequestParam("publishTime") String publishTime,
                                             @RequestParam("price") String price) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (bookService.PrototypeUpload(multipartFile, bookName, ISBN, author, intro, publishTime, price)) {
            map.put("message", "success");
        } else {
            map.put("message", "ISBN冲突,书本已存在");
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping(value = "/system/addCopy")
    public ResponseEntity<?> addCopy(@RequestParam("token") String token,
                                     @RequestParam("bookNum") Integer bookNum,
                                     @RequestParam("ISBN") String ISBN) {
        Map<String, String> map = new HashMap<>();
        int ans = bookService.AddCopy(token, bookNum, ISBN);
        if (ans == 1) {
            map.put("message", "ISBN不存在，请先上传书本");
        } else if (ans == 2) {
            map.put("message", "数量错误");
        } else {
            map.put("message", "success");
        }
        return ResponseEntity.ok(map);
    }

    /*************************书本操作的逻辑在此************************/
    @PostMapping(value = "/system/borrow")
    public ResponseEntity<?> copyBorrow(@RequestParam("cid") String[] cid,
                                        @RequestParam(value = "borrowerId") String id,
                                        @RequestParam("token") String token) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        if (checkParamsInvalid(id, map)) {
            return ResponseEntity.ok(map);
        }
        return ResponseEntity.ok(bookService.CopyBorrow(cid, id, token));
    }

    @PostMapping(value = "/system/getReserved")
    public ResponseEntity<?> CopyGetReserved(@RequestParam("cid") String[] cid,
                                             @RequestParam(value = "borrowerId") String id,
                                             @RequestParam("token") String token) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        if (checkParamsInvalid(id, map)) {
            return ResponseEntity.ok(map);
        }
        return ResponseEntity.ok(bookService.CopyGetReserved(cid, id, token));
    }

    @PostMapping(value = "/system/return")
    public ResponseEntity<?> CopyReturn(@RequestParam("cid") String[] cid,
                                        @RequestParam("token") String token,
                                        @RequestParam("state") String[] state) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        if (bookService.CopyReturn(cid, token, state)) {
            map.put("message", "success");
        } else {
            map.put("message", "归还失败");
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping(value = "books/reserve")
    public ResponseEntity<?> CopyReserve(@RequestParam("cid") String[] cid,
                                         @RequestParam(value = "token") String token) {
        return ResponseEntity.ok(bookService.CopyReserve(cid, token));
    }

    private boolean checkParamsInvalid(String id, Map<String, Object> map) {
        //返回false表示可以操作了，返回true直接让主函数中断掉
        boolean flag = false;
        if (userRepository.findByUsername(id) == null) {
            map.put("message", "用户不存在");
            flag = true;
        } else if (userRepository.findByUsername(id).getAuthoritiesString().contains("Librarian")) {
            map.put("message", "用户无权操作");
            flag = true;
        }
        return flag;
    }

    @PostMapping(value = "/reader/comment")
    public ResponseEntity<?> comment(@RequestParam("star") Integer star,
                                     @RequestParam("content")  String content,
                                     @RequestParam("ISBN") String ISBN,
                                     @RequestParam("token") String token) {
        Map<String, Object> map = new HashMap<>();
        if(bookService.comment(star , content , ISBN , token)){
            map.put("message", "success");
        }
        else {
            map.put("message", "评论失败");
        }
        return ResponseEntity.ok(map);
    }


    @PostMapping(value = "/reader/discussion")
    public ResponseEntity<?> discussion(@RequestParam("content")  String content,
                                        @RequestParam("reply") String reply,
                                        @RequestParam("token") String token,
                                        @RequestParam("commentId") Long commentId) {
        Map<String, Object> map = new HashMap<>();
        if(bookService.discussion(content , reply , token , commentId)){
            map.put("message", "success");
        }
        else {
            map.put("message", "评论失败");
        }
        return ResponseEntity.ok(map);
    }


}
