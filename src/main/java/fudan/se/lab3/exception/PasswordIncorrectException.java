package fudan.se.lab3.exception;

public class PasswordIncorrectException extends RuntimeException {
    public PasswordIncorrectException(String username) {
        super("Username '" + username + "' password incorrect");
    }
}
