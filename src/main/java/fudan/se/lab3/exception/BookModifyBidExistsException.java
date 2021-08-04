package fudan.se.lab3.exception;

public class BookModifyBidExistsException extends RuntimeException{
    public BookModifyBidExistsException(){super("Bid of your uploaded book exists");}
}
