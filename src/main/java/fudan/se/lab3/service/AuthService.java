package fudan.se.lab3.service;

import fudan.se.lab3.controller.request.AddRequest;
import fudan.se.lab3.controller.request.LoginRequest;
import fudan.se.lab3.domain.book.Comment;
import fudan.se.lab3.domain.book.Discussion;
import fudan.se.lab3.domain.people.Attributes;
import fudan.se.lab3.domain.people.Reader;
import fudan.se.lab3.exception.BadAuthRequestException;
import fudan.se.lab3.exception.PasswordIncorrectException;
import fudan.se.lab3.repository.*;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import fudan.se.lab3.domain.people.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import fudan.se.lab3.common.Utils;

import java.util.*;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final AttributesRepository attributesRepository;
    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final ReaderRepository readerRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthService(UserRepository userRepository, AuthorityRepository authorityRepository, AttributesRepository attributesRepository
            , CommentRepository commentRepository, DiscussionRepository discussionRepository, ReaderRepository readerRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.attributesRepository = attributesRepository;
        this.commentRepository = commentRepository;
        this.discussionRepository = discussionRepository;
        this.readerRepository = readerRepository;
    }

    public Map<String, Object> login(LoginRequest loginRequest) throws UsernameNotFoundException, PasswordIncorrectException, BadAuthRequestException {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String region = loginRequest.getRegion();
        String type = loginRequest.getType();
        Map<String, Object> result = new HashMap<>();
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (userRepository.findByUsername(loginRequest.getUsername()) == null) {
                throw new UsernameNotFoundException(username);
            } else if (!(new BCryptPasswordEncoder()).matches(password, userRepository.findByUsername(username).getPassword())) {
                throw new PasswordIncorrectException(username);
            }
        }
        User user = (User) authentication.getPrincipal();
        if ("user".equals(type) && user.getAuthoritiesString().contains("Reader")) {
            region = null;
            result.put("role", "User");
        } else if ("admin".equals(type) && !Utils.locationInvalid(region)) {
            //admin part: Librarian and SuperLibrarian
            if (user.getAuthoritiesString().contains("Librarian")) {
                result.put("role", "Librarian");
            } else if (user.getAuthoritiesString().contains("SuperLibrarian")) {
                result.put("role", "SuperLibrarian");
            } else {
                // just a user
                throw new BadAuthRequestException();
            }
        } else {
            //check for invalid type & no-auth situation
            throw new BadAuthRequestException();
        }
        result.put("token", jwtTokenUtil.generateToken(user, region));
        return result;
    }

    public Map<String, Object> add(AddRequest request) {
        Map<String, Object> result = new HashMap<>();
        String username = request.getUsername();
        String password = request.getPassword();
        if (Utils.isAdminInvalid(username, password, "admin")) {
            result.put("message", "Illegal username or password");
            return result;
        }
        User user = userRepository.findByUsername(username);
        if (user != null) {
            result.put("message", "Duplicated username not allowed");
        } else {
            userRepository.save(new User(username, (new BCryptPasswordEncoder()).encode(password),
                    new HashSet<>(Collections.singletonList(authorityRepository.findByAuthority("Librarian")))));
            result.put("message", "success");
        }
        return result;
    }

    public Map<String, Object> changePassword(String password, String rePassword, String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findByUsername(jwtTokenUtil.getUsernameFromToken(token));
        if (user == null) {
            result.put("message", "You are using a bad credential");
            return result;
        }
        if (!(new BCryptPasswordEncoder()).matches(password, user.getPassword())) {
            result.put("message", "Your first input must match your original password");
            return result;
        }
        //separate two cases user or admin
        boolean flag1 = false, flag2 = false;
        if (user.getAuthoritiesString().contains("Reader")) {
            flag1 = Utils.isReaderInvalid(user.getUsername(), rePassword);
        } else if (user.getAuthoritiesString().contains("Librarian") || user.getAuthoritiesString().contains("SuperLibrarian")) {
            flag2 = Utils.isAdminInvalid(user.getUsername(), rePassword, "admin");
        }
        if (flag1) {
            result.put("message", "Illegal password, 6-32 digits, letters - or _, excluding username, containing two kinds at least");
        } else if (flag2) {
            result.put("message", "Illegal password, 6-32 digits or letters");
        } else {
            user.setPassword((new BCryptPasswordEncoder()).encode(rePassword));
            userRepository.save(user);
            result.put("message", "success");
        }

        return result;
    }

    public Map<String, Object> alterAttributes(String type, int maxBorrow, String reserveDuration, String borrowDuration) {
        Map<String, Object> result = new HashMap<>();
        if (Utils.isReaderTypeInvalid(type)) {
            result.put("message", "您选择了不存在的用户类型");
            return result;
        }
        Attributes attr = this.attributesRepository.findByType(type);
        attr.setMaxBorrow(maxBorrow);
        attr.setBorrowDuration(borrowDuration);
        attr.setReserveDuration(reserveDuration);
        this.attributesRepository.save(attr);
        result.put("message", "success");
        return result;
    }

    public Map<String, Object> resetScore(String username) {
        Map<String, Object> map = new HashMap<>();
        Reader reader = readerRepository.findByUsername(username);
        if(reader == null){
            map.put("message", "用户不存在！");
            return map;
        }
        reader.resetScore();
        readerRepository.save(reader);
        map.put("message", "success");
        return map;
    }

    public Map<String, Object> deleteBadComment(Long commentId) {
        Map<String, Object> map = new HashMap<>();
        Comment comment = commentRepository.findCommentById(commentId);
        if(comment == null){
            map.put("message", "评论不存在！");
            return map;
        }
        comment.setBad(true);
        commentRepository.save(comment);
        map.put("message", "success");
        return map;
    }

    public Map<String, Object> deleteBadDiscussion(Long discussionId) {
        Map<String, Object> map = new HashMap<>();
        Discussion discussion = discussionRepository.findDiscussionById(discussionId);
        if(discussion == null){
            map.put("message", "讨论不存在！");
            return map;
        }
        discussion.setBad(true);
        discussionRepository.save(discussion);
        map.put("message", "success");
        return map;
    }
}
