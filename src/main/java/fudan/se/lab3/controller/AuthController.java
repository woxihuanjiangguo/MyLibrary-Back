package fudan.se.lab3.controller;

import fudan.se.lab3.controller.request.AddRequest;
import fudan.se.lab3.controller.request.CodeRequest;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import fudan.se.lab3.service.AuthService;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.controller.request.RegisterRequest;
import fudan.se.lab3.service.BookService;
import fudan.se.lab3.service.ReaderService;
import fudan.se.lab3.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author LBW
 */
@RestController
public class AuthController {

    private final AuthService authService;
    private final ReaderService readerService;
    private final SearchService searchService;
    private final BookService bookService;
    private final JwtTokenUtil jwtTokenUtil;

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(BookService bookService, SearchService searchService, AuthService authService, ReaderService readerService, JwtTokenUtil jwtTokenUtil) {
        this.bookService = bookService;
        this.searchService = searchService;
        this.authService = authService;
        this.readerService = readerService;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.debug("RegistrationForm: " + request.toString());
        return ResponseEntity.ok(readerService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.debug("LoginForm: " + request.toString());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/system/add")
    public ResponseEntity<?> add(@RequestBody AddRequest request){
        logger.debug("AddForm: " + request.toString());
        return ResponseEntity.ok(authService.add(request));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String password,
                                            @RequestParam String rePassword,
                                            @RequestParam String token){
        logger.debug("Change password: User token = " + token);
        return ResponseEntity.ok(authService.changePassword(password,rePassword,token));
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<?> sendEmail(@RequestBody CodeRequest request){
        logger.debug("SendEmail: " + request.toString());
        return ResponseEntity.ok(readerService.code(request));
    }

    //用户付款
    @PostMapping("/reader/pay")
    public ResponseEntity<?> payFine(@RequestParam String token, @RequestParam String cid){
        logger.debug("Reader pays fine: User token = " + token);
        return ResponseEntity.ok(readerService.payFine(token,cid));
    }

    //超管控制一些属性
    @PostMapping("/system/search/attributes")
    public ResponseEntity<?> getAttributes(){
        return ResponseEntity.ok(this.searchService.getAttributes());
    }

    @PostMapping("/system/alterAttributes")
    public ResponseEntity<?> alterAttributes(@RequestParam String type,
                                             @RequestParam int maxBorrow,
                                             @RequestParam String reserveDuration,
                                             @RequestParam String borrowDuration){
        return ResponseEntity.ok(this.authService.alterAttributes(type,maxBorrow,reserveDuration,borrowDuration));
    }

    //超管发出统一的提醒
    @PostMapping(value = "/system/checkOverdue")
    public ResponseEntity<?> checkOverdue() throws ParseException {
        Map<String, Object> map = new HashMap<>();
        bookService.CheckOverdue();
        map.put("message", "success");
        return ResponseEntity.ok(map);
    }

    /*****************************用户部分结束**********************************/

    /*****************************查询部分开启**********************************/


    /*************用户只需要两个*************/
    //  给用户 统一的提醒 showNotice
    @PostMapping("/reader/search/showNotice")
    public ResponseEntity<?> readerNotice(@RequestParam("token") String token){
        Map<String, Object> result = new HashMap<>();
        result.put("copyListUnpaid", searchService.copySearchbyTag(token));
        result.put("copyListHandle", searchService.CopySearchbyUser(token));
        return ResponseEntity.ok(result);
    }

    // 给用户 统一的信息 userInfo
    @PostMapping("/reader/search/userInfo")
    public ResponseEntity<?> readerInfo(@RequestParam("token") String token){
        Map<String, Object> result = new HashMap<>();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        result.put("logList", searchService.LogSearchbyReader(username));
        result.put("copyList", searchService.copySearchbyTag(token));
        result.put("payLogList", searchService.readerPayLogs(username));
        result.put("basicInfo", searchService.searchOneReader(username).get(0));
        return ResponseEntity.ok(result);
    }

    /*************管理员比较多*************/
    //管理员查询自己的记录
    @PostMapping("/system/search/myLog")
    public ResponseEntity<?> adminLogs(@RequestParam String token){
        Map<String, Object> result = new HashMap<>();
        result.put("logList", this.searchService.adminLogs(token));
        return ResponseEntity.ok(result);
    }

    //管理员根据用户名查他的操作记录 --- 付款 加 书本
    @PostMapping("/system/search/readerLog")
    public ResponseEntity<?> readerLogs(@RequestParam String username){
        logger.debug("Admin searches a user book logs: Username = " + username);
        Map<String, Object> result = new HashMap<>();
        result.put("logList", this.searchService.LogSearchbyReader(username));
        result.put("payLogList", this.searchService.readerPayLogs(username));
        return ResponseEntity.ok(result);
    }

    //管理员根据用户名查他进行中的事务
    @PostMapping("/system/search/readerOperation")
    public ResponseEntity<?> readerOperations(@RequestParam String username){
        logger.debug("Admin searches a user operation: Username = " + username);
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", this.searchService.copySearchbyUsername(username));
        result.put("unpaidList", this.searchService.copySearchbyTagUsername(username));
        return ResponseEntity.ok(result);
    }

    //管根据用户名查询指定用户
    @PostMapping("/system/search/oneUser")
    public ResponseEntity<?> searchOneReader(String username){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchOneReader(username));
        return ResponseEntity.ok(result);
    }

    //管根据用户类型查询指定用户
    @PostMapping("/system/search/oneTypeUser")
    public ResponseEntity<?> searchOneTypeUser(String type){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchOneTypeUser(type));
        return ResponseEntity.ok(result);
    }

    //管查询所有用户
    @PostMapping("/system/search/allUsers")
    public ResponseEntity<?> searchAllUsers(){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchAllUsers());
        return ResponseEntity.ok(result);
    }

    /*****************************新增******************************/
    //用户查询自己的评论与讨论以及未评论
    @PostMapping("/reader/search/commentAndDiscussion")
    public ResponseEntity<?> readerSearchSayings(@RequestParam String token){
        Map<String, Object> result = new HashMap<>();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        result.put("commentList", searchService.searchOneReaderComment(username));
        result.put("uncommentedList", searchService.searchOneReaderUncommented(jwtTokenUtil.getUsernameFromToken(token)));
        return ResponseEntity.ok(result);
    }

    //管理员查询某个读者的评论与讨论
    @PostMapping("/system/search/commentAndDiscussion")
    public ResponseEntity<?> systemSearchSayings(@RequestParam String username){
        Map<String, Object> result = new HashMap<>();
        result.put("commentList", searchService.searchOneReaderComment(username));
        return ResponseEntity.ok(result);
    }

    //管理员删评论
    @PostMapping("/system/deleteBadComment")
    public ResponseEntity<?> deleteBadComment(@RequestParam Long commentId){
        return ResponseEntity.ok(authService.deleteBadComment(commentId));
    }

    //管理员删讨论
    @PostMapping("/system/deleteBadDiscussion")
    public ResponseEntity<?> deleteBadDiscussion(@RequestParam Long discussionId){
        return ResponseEntity.ok(authService.deleteBadDiscussion(discussionId));
    }

    //超管理员大赦，重置信用
    @PostMapping("/system/reset")
    public ResponseEntity<?> resetScore(@RequestParam String username){
        return ResponseEntity.ok(authService.resetScore(username));
    }
}
