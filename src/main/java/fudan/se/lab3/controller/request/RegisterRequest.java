package fudan.se.lab3.controller.request;

/**
 * @author LBW
 */
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String readerType;
    private String securityCode;

    public RegisterRequest(String username, String password, String email, String readerType,String securityCode) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.readerType = readerType;
        this.securityCode = securityCode;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getReaderType() {
        return readerType;
    }
}

