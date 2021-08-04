package fudan.se.lab3.controller.request;

public class AddRequest {
    private String username;
    private String password;

    public AddRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
