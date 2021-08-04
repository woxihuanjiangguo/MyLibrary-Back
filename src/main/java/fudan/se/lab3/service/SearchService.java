package fudan.se.lab3.service;

import fudan.se.lab3.domain.book.*;
import fudan.se.lab3.domain.people.*;
import fudan.se.lab3.repository.*;
import fudan.se.lab3.security.jwt.JwtTokenUtil;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SearchService {

    private final UserRepository userRepository;
    private final CopyRepository copyRepository;
    private final PrototypeRepository prototypeRepository;
    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final ReaderRepository readerRepository;
    private final AttributesRepository attributesRepository;
    private final JwtTokenUtil jwtTokenUtil;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SearchService(UserRepository userRepository, CopyRepository copyRepository , PrototypeRepository prototypeRepository , CommentRepository commentRepository , DiscussionRepository discussionRepository , ReaderRepository readerRepository ,AttributesRepository attributesRepository, JwtTokenUtil jwtTokenUtil){
        this.userRepository = userRepository;
        this.copyRepository = copyRepository;
        this.prototypeRepository = prototypeRepository;
        this.commentRepository = commentRepository;
        this.discussionRepository = discussionRepository;
        this.readerRepository = readerRepository;
        this.attributesRepository = attributesRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public Copy CopySearchbyCid(String cid){
        return copyRepository.findByCid(cid);
    }

    public Prototype PrototypeSearchbyISBN(String ISBN){
        return prototypeRepository.findByISBN(ISBN);
    }

    public List<Copy> CopySearchbyISBNCopy(String ISBN){
        return copyRepository.findByPrototype(prototypeRepository.findByISBN(ISBN));
    }

    public List<Copy> CopySearchbyReserver(String reserver){
        return copyRepository.findByReserverId(reserver);
    }

    public List<Prototype> PrototypeSearchbyAuthorlike(String author){
        return prototypeRepository.findByAuthorLike("%" + author + "%");
    }

    public List<Prototype> PrototypeSearchbyBookNamelike(String name){
        return prototypeRepository.findByBookNameLike("%"+name+"%");
    }

    public List<Copy> CopySearchbyUser(String token){
        String id = jwtTokenUtil.getUsernameFromToken(token);
        List<Copy> copyList = copyRepository.findByReserverId(id);
        copyList.addAll(copyRepository.findByBorrowerId(id));
        return copyList;
    }

    public List<Copy> copySearchbyUsername(String username){
        List<Copy> copyList = copyRepository.findByReserverId(username);
        copyList.addAll(copyRepository.findByBorrowerId(username));
        return copyList;
    }

    public List<Copy> CopySearchbyAdmin(String token){
        List<Copy> copyList = copyRepository.findAll();
        List<Copy> list = new ArrayList<>();
        String id = jwtTokenUtil.getUsernameFromToken(token);
        for(Copy copy : copyList){
            List<Log> logList = copy.getLogList();
            boolean flag = false;
            for(Log log : logList){
                if(log.getBorrowerId() != null && (log.getBorrowAdmin().equals(id) || log.getReturnAdmin().equals(id))){
                    flag = true;
                    break;
                }
            }
            if(flag){
                list.add(copy);
            }
        }
        return list;
    }

    public List<Log> LogSearchbyReader(String username){  //改成username可以复用
        List<Copy> copyList = copyRepository.findAll();
        List<Log> logList = new ArrayList<>();
        for(Copy copy : copyList){
            for(Log log : copy.getLogList()){
                if(log == null){
                    continue;
                }
                if((log.getReserverId() != null && log.getReserverId().equals(username)) || (log.getBorrowerId() != null && log.getBorrowerId().equals(username))){
                    logList.add(log);
                }
            }
        }
        return logList;
    }

    public List<Copy> copySearchAllbyOverdue() { //管理员搜索所有借阅过期
        List<Copy> copyList = copyRepository.findAll();
        return copyList;
    }

    public List<Copy> copySearchAllbyWhichOverdue(int type) { //管理员搜索所有借阅过期 type 1借阅，2预约
        List<Copy> copyList = copyRepository.findAll();
        List<Copy> result = new ArrayList<>();
        Date date = new Date();
        for(Copy copy : copyList){
            try {
                if (type == 1 && date.compareTo(format.parse(copy.getExpectedReturnTime())) > 0) {
                    result.add(copy);
                }
                if(type == 2 && date.compareTo(format.parse(copy.getExpectedBorrowTime())) > 0){
                    result.add(copy);
                }
            }catch (Exception e){
            }
        }
        return result;
    }

    public List<Copy> copySearchbyTagAll(){ //管理员搜索所有未交罚款
        List<Copy> copyList = copyRepository.findAll();
        List<Copy> list = new ArrayList<>();
        for(Copy copy : copyList){
            if(copy.getTag() != null && !"".equals(copy.getTag())){
                list.add(copy);
            }
        }
        return list;
    }

    public List<Copy> copySearchbyTag(String token){//用户搜索所有未交罚款
        List<Copy> copies = copyRepository.findByTag(jwtTokenUtil.getUsernameFromToken(token));
        for (Copy copy : copies) {
            copy.setTempPrice();
        }
        return copies;
    }

    public List<Copy> copySearchbyTagUsername(String username){//用户搜索所有未交罚款
        List<Copy> copies = copyRepository.findByTag(username);
        for (Copy copy : copies) {
            copy.setTempPrice();
        }
        return copies;
    }

    public List<Prototype> PrototypeSearchDefaultAll(String number){
        if("default".equals(number)){
            return prototypeRepository.findAll();
        }
        return null;
    }

    public List<Log> adminLogs(String token){
        String adminUsername = jwtTokenUtil.getUsernameFromToken(token);
        List<Copy> copyList = copyRepository.findAll();
        List<Log> logList = new ArrayList<>();
        for(Copy copy : copyList){
            for(Log log : copy.getLogList()){
                if(log == null){
                    continue;
                }
                if(adminUsername.equals(log.getBorrowAdmin()) || adminUsername.equals(log.getReturnAdmin())){
                    logList.add(log);
                }
            }
        }
        return logList;
    }

    public List<PayLog> readerPayLogs(String username){
        Reader reader = readerRepository.findByUsername(username);
        List<PayLog> temp = new ArrayList<>(reader.getPayLogList());
        return temp;
    }

    public Map<String,Object> getAttributes(){
        Map<String, Object> result = new HashMap<>();
        Attributes attr1 = attributesRepository.findByType("Teacher");
        Attributes attr2 = attributesRepository.findByType("Postgraduate");
        Attributes attr3 = attributesRepository.findByType("Undergraduate");
        result.put("attr1", attr1);
        result.put("attr2", attr2);
        result.put("attr3", attr3);
        return result;
    }

    //超管根据用户名查询指定用户
    public List<Reader> searchOneReader(String username){
        List<Reader> temp = new ArrayList<>();
        temp.add(readerRepository.findByUsername(username));
        return temp;
    }
    //超管根据用户类型查询指定用户
    public List<Reader> searchOneTypeUser(String type){
        Attributes attr = attributesRepository.findByType(type);
        if(attr == null){
            return null;
        }
        return readerRepository.findAllByAttributes(attr);
    }
    //超管查询所有用户
    public List<Reader> searchAllUsers(){
        return readerRepository.findAll();
    }

    //查看用户评论记录 将用户的discussion也放到这里，呈现出上下文即可
    public List<Comment> searchOneReaderComment(String username){
        Reader reader = readerRepository.findByUsername(username);
        List<Comment> commentList = commentRepository.findCommentsByReader(reader);
        List<Discussion> discussionList = discussionRepository.findDiscussionsByReader(reader);
        for(Discussion discussion : discussionList){
            if(!commentList.contains(discussion.getComment())){
                commentList.add(discussion.getComment());
            }
        }
        return commentList;
    }


    //查看用户未评论原型
    public Set<Prototype> searchOneReaderUncommented(String username){
        return readerRepository.findByUsername(username).getNotCommentedPrototypeList();
    }
}
