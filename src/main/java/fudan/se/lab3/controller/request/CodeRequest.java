package fudan.se.lab3.controller.request;

public class CodeRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public CodeRequest(){}

    public CodeRequest(String email){
        this.email = email;
    }
}
