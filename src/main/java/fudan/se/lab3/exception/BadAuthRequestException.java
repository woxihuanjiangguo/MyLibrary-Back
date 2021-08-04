package fudan.se.lab3.exception;

public class BadAuthRequestException extends RuntimeException {
    public BadAuthRequestException() {
        super("Bad auth type requested or bad location selected");
    }
}
