package fudan.se.lab3.common;

import fudan.se.lab3.controller.request.RegisterRequest;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Utils {

    public static boolean locationInvalid(String singleSource){
        return !("邯郸".equals(singleSource) || "枫林".equals(singleSource) || "张江".equals(singleSource) || "江湾".equals(singleSource));
    }

    public static boolean isReaderTypeInvalid(String type){
        Set<String> allowedTypes = new HashSet<>();
        allowedTypes.add("Postgraduate");
        allowedTypes.add("Undergraduate");
        allowedTypes.add("Teacher");

        //不包含则invalid 返回true
        return !allowedTypes.contains(type);
    }

    public static boolean registerIsInvalid(RegisterRequest registerRequest){
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String email = registerRequest.getEmail();
        String readerType = registerRequest.getReaderType();

        if (isReaderTypeInvalid(readerType) || isReaderInvalid(username,password)) {
            return true;
        }

        //邮箱正则
        if(!email.matches("^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")){
            return true;
        }else {
            String[] parts = email.split("@");
            return !username.equals(parts[0]) || !"fudan.edu.cn".equals(parts[1]);
        }
    }

    /* Constantly used checkers
    *  username, password (separate user and admin)
    *  email (one user)
    *  普通的读者
    * */
    public static boolean isReaderInvalid(String username,String password){
        //空的字符串与长度
        if(username == null || password == null){
            return true;
        }
        //密码中含有用户名
        boolean flag = password.contains(username);
        //用户名正则
        if(!username.matches("^[0-9]{11}$")){
            flag = true;
        }
        //密码正则
        if(!password.matches("[A-Za-z0-9_-]{6,32}")){
            //含有别的东西
            flag = true;
        }
        //种类太少
        if(password.matches("^[A-Za-z]+$") || password.matches("^[0-9]+$")){
            flag = true;
        }
        if(password.matches("^[-]+$") || password.matches("^[_]+$")){
            flag = true;
        }
        return flag;
    }

    public static boolean isAdminInvalid(String username,String password,String superName){
        if(superName.equals(username)){
            return !password.matches("[a-zA-Z0-9]{6,32}");
        }else{
            return !username.matches("[a-zA-Z0-9]{6,32}") || !password.matches("[a-zA-Z0-9]{6,32}");
        }
    }

    public static String codeGenerator() throws NoSuchAlgorithmException {
        String result = "";
        for(int i = 0; i < 6;i++){
            Random r = SecureRandom.getInstanceStrong();
            int num = r.nextInt(10);
            result += num;
        }
        return result;
    }


    public static boolean isStrNotNull(String str){
        return (str != null) && (!"".equals(str));
    }
    //后续可加入更多的静态方法
}
