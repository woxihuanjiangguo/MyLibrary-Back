package fudan.se.lab3.exception;

public class BookDeleteFailException extends RuntimeException{
    public BookDeleteFailException(){super("The book you try to delete does not exist");}
}
