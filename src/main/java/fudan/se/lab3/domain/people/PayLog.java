package fudan.se.lab3.domain.people;

import com.fasterxml.jackson.annotation.JsonProperty;
import fudan.se.lab3.domain.book.Copy;
import fudan.se.lab3.domain.book.PicFile;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class PayLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long payLogId;

    @JsonProperty("cid")
    private String cid;

    @JsonProperty("isbn")
    private String ISBN;

    @JsonProperty("bookName")
    private String bookName;

    @ManyToOne(targetEntity = PicFile.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "paylog_picfile",
            joinColumns = @JoinColumn(name = "payLogId", unique = true),
            inverseJoinColumns = @JoinColumn(name = "fileId"))
    @JsonProperty("picFile")
    private PicFile picFile;

    @JsonProperty("pricePaid")
    private double pricePaid;

    @JsonProperty("payTime")
    private String payTime;

    @JsonProperty("cause")
    private String cause;

    public PicFile getPicFile() {
        return picFile;
    }

    public PayLog(){

    }

    public PayLog(Copy copy, double pricePaid,String payTime, String cause){
        this.cid = copy.getCid();
        this.ISBN = copy.getPrototype().getISBN();
        this.bookName = copy.getPrototype().getBookName();
        this.picFile = copy.getPrototype().getPicFile();
        this.pricePaid = pricePaid;
        this.payTime = payTime;
        this.cause = cause;
    }
}
