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

    //????????????
    @PostMapping("/reader/pay")
    public ResponseEntity<?> payFine(@RequestParam String token, @RequestParam String cid){
        logger.debug("Reader pays fine: User token = " + token);
        return ResponseEntity.ok(readerService.payFine(token,cid));
    }

    //????????????????????????
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

    //???????????????????????????
    @PostMapping(value = "/system/checkOverdue")
    public ResponseEntity<?> checkOverdue() throws ParseException {
        Map<String, Object> map = new HashMap<>();
        bookService.CheckOverdue();
        map.put("message", "success");
        return ResponseEntity.ok(map);
    }

    /*****************************??????????????????**********************************/

    /*****************************??????????????????**********************************/


    /*************?????????????????????*************/
    //  ????????? ??????????????? showNotice
    @PostMapping("/reader/search/showNotice")
    public ResponseEntity<?> readerNotice(@RequestParam("token") String token){
        Map<String, Object> result = new HashMap<>();
        result.put("copyListUnpaid", searchService.copySearchbyTag(token));
        result.put("copyListHandle", searchService.CopySearchbyUser(token));
        return ResponseEntity.ok(result);
    }

    // ????????? ??????????????? userInfo
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

    /*************??????????????????*************/
    //??????????????????????????????
    @PostMapping("/system/search/myLog")
    public ResponseEntity<?> adminLogs(@RequestParam String token){
        Map<String, Object> result = new HashMap<>();
        result.put("logList", this.searchService.adminLogs(token));
        return ResponseEntity.ok(result);
    }

    //????????????????????????????????????????????? --- ?????? ??? ??????
    @PostMapping("/system/search/readerLog")
    public ResponseEntity<?> readerLogs(@RequestParam String username){
        logger.debug("Admin searches a user book logs: Username = " + username);
        Map<String, Object> result = new HashMap<>();
        result.put("logList", this.searchService.LogSearchbyReader(username));
        result.put("payLogList", this.searchService.readerPayLogs(username));
        return ResponseEntity.ok(result);
    }

    //????????????????????????????????????????????????
    @PostMapping("/system/search/readerOperation")
    public ResponseEntity<?> readerOperations(@RequestParam String username){
        logger.debug("Admin searches a user operation: Username = " + username);
        Map<String, Object> result = new HashMap<>();
        result.put("copyList", this.searchService.copySearchbyUsername(username));
        result.put("unpaidList", this.searchService.copySearchbyTagUsername(username));
        return ResponseEntity.ok(result);
    }

    //????????????????????????????????????
    @PostMapping("/system/search/oneUser")
    public ResponseEntity<?> searchOneReader(String username){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchOneReader(username));
        return ResponseEntity.ok(result);
    }

    //???????????????????????????????????????
    @PostMapping("/system/search/oneTypeUser")
    public ResponseEntity<?> searchOneTypeUser(String type){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchOneTypeUser(type));
        return ResponseEntity.ok(result);
    }

    //?????????????????????
    @PostMapping("/system/search/allUsers")
    public ResponseEntity<?> searchAllUsers(){
        Map<String, Object> result = new HashMap<>();
        result.put("readerList", searchService.searchAllUsers());
        return ResponseEntity.ok(result);
    }

    /*****************************??????******************************/
    //???????????????????????????????????????????????????
    @PostMapping("/reader/search/commentAndDiscussion")
    public ResponseEntity<?> readerSearchSayings(@RequestParam String token){
        Map<String, Object> result = new HashMap<>();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        result.put("commentList", searchService.searchOneReaderComment(username));
        result.put("uncommentedList", searchService.searchOneReaderUncommented(jwtTokenUtil.getUsernameFromToken(token)));
        return ResponseEntity.ok(result);
    }

    //?????????????????????????????????????????????
    @PostMapping("/system/search/commentAndDiscussion")
    public ResponseEntity<?> systemSearchSayings(@RequestParam String username){
        Map<String, Object> result = new HashMap<>();
        result.put("commentList", searchService.searchOneReaderComment(username));
        return ResponseEntity.ok(result);
    }

    //??????????????????
    @PostMapping("/system/deleteBadComment")
    public ResponseEntity<?> deleteBadComment(@RequestParam Long commentId){
        return ResponseEntity.ok(authService.deleteBadComment(commentId));
    }

    //??????????????????
    @PostMapping("/system/deleteBadDiscussion")
    public ResponseEntity<?> deleteBadDiscussion(@RequestParam Long discussionId){
        return ResponseEntity.ok(authService.deleteBadDiscussion(discussionId));
    }

    //?????????????????????????????????
    @PostMapping("/system/reset")
    public ResponseEntity<?> resetScore(@RequestParam String username){
        return ResponseEntity.ok(authService.resetScore(username));
    }
}
