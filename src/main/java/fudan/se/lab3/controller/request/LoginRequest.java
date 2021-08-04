package fudan.se.lab3.controller.request;

/**
 * @author LBW
 */
public class LoginRequest {
    private String username;
    private String password;
    private String region;
    private String type;

    public LoginRequest(String username, String password, String region , String type) {
        this.username = username;
        this.password = password;
        this.region = region;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getRegion() {
        return region;
    }

    public String getType() {
        return type;
    }
}
