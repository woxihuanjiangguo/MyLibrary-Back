package fudan.se.lab3.domain.book;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Log implements Serializable {
    @Id
    @Column(name = "logId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long logId;                 // 避免一个cid多次记录

    private String cid;
    @JsonProperty("isbn")
    private String ISBN;
    @JsonProperty("bookName")
    private String bookName;

    @ManyToOne(targetEntity = PicFile.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "log_picfile",
            joinColumns = @JoinColumn(name = "logId", unique = true),
            inverseJoinColumns = @JoinColumn(name = "fileId"))
    @JsonProperty("picFile")
    private PicFile picFile;

    // 预约信息
    private String reserverId;
    private String reservedTime;
    // 借阅信息
    private String borrowerId;
    private String borrowedTime;
    private String borrowAdmin;
    private String borrowRegion;
    // 归还信息
    private String returnTime;
    private String returnAdmin;
    private String returnRegion;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getReserverId() {
        return reserverId;
    }

    public void setReserverId(String reserverId) {
        this.reserverId = reserverId;
    }

    public String getReservedTime() {
        return reservedTime;
    }

    public void setReservedTime(String reservedTime) {
        this.reservedTime = reservedTime;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowedTime() {
        return borrowedTime;
    }

    public void setBorrowedTime(String borrowedTime) {
        this.borrowedTime = borrowedTime;
    }

    public String getBorrowAdmin() {
        return borrowAdmin;
    }

    public void setBorrowAdmin(String borrowAdmin) {
        this.borrowAdmin = borrowAdmin;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public String getReturnAdmin() {
        return returnAdmin;
    }

    public void setReturnAdmin(String returnAdmin) {
        this.returnAdmin = returnAdmin;
    }

    public void setBorrowRegion(String borrowRegion) {
        this.borrowRegion = borrowRegion;
    }

    public void setReturnRegion(String returnRegion) {
        this.returnRegion = returnRegion;
    }

    public String getBorrowRegion() {
        return borrowRegion;
    }

    public String getReturnRegion() {
        return returnRegion;
    }

    public PicFile getPicFile() {
        return picFile;
    }

    public Log(){
    }

    public Log(Copy copy){
        this.cid = copy.getCid();
        this.ISBN = copy.getPrototype().getISBN();
        this.bookName = copy.getPrototype().getBookName();
        this.picFile = copy.getPrototype().getPicFile();
    }
}
