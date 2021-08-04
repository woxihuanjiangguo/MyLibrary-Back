package fudan.se.lab3.exception;

public class BookModifyBidExitsException extends RuntimeException{
    public BookModifyBidExitsException(){super("Bid of your uploaded book exists");}
}
