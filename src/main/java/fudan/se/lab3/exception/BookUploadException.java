package fudan.se.lab3.exception;

public class BookUploadException extends RuntimeException{
    public BookUploadException(){super("Information in your uploaded book conflicts with existing books with the same ISBN");}

}
