package fudan.se.lab3.domain.book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Copy implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long copyId;

    private String cid;
    private String region;
    private String state;   // available, reserved, borrowed, damaged, lost
    private String reserverId;
    private String expectedBorrowTime;
    private String borrowerId;
    private String expectedReturnTime;
    private String contributor;
    private String tag;

    @JsonProperty("tempPrice")
    private double tempPrice;

    @ManyToOne(targetEntity = Prototype.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "copy_prototype",
            joinColumns = @JoinColumn(name = "cid",unique = true),
            inverseJoinColumns = @JoinColumn(name = "prototypeId"))
    private Prototype prototype;

    @OneToMany(targetEntity = Log.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "copy_log",
                joinColumns = @JoinColumn(name = "cid"),
                inverseJoinColumns = @JoinColumn(name = "logId"))
    @JsonIgnore
    private List<Log> logList = new ArrayList<>();

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReserverId() {
        return reserverId;
    }

    public void setReserverId(String reserverId) {
        this.reserverId = reserverId;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getExpectedBorrowTime() {
        return expectedBorrowTime;
    }

    public String getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public List<Log> getLogList() {
        return logList;
    }

    public String isTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getTag() {
        return tag;
    }

    public void setExpectedBorrowTime(String expectedBorrowTime) {
        this.expectedBorrowTime = expectedBorrowTime;
    }

    public void setExpectedReturnTime(String expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    // 设置一个临时的付款金额
    public void setTempPrice(){
        this.tempPrice = this.prototype.getPrice() * this.ratio();
    }

    public double getTempPrice(){
        return tempPrice;
    }

    public Prototype getPrototype() {
        return prototype;
    }

    public void setPrototype(Prototype prototype) {
        this.prototype = prototype;
    }

    private double ratio(){
        double ratio = 0;
        switch (this.state){
            //晚还
            case "available" :
                ratio = 0.25;
                break;
            case "damaged" :
                ratio = 0.5;
                break;
            case "lost" :
                ratio = 1;
                break;
        }
        return ratio;
    }

    public Copy() {

    }

    public Copy(Prototype prototype , String region , String state , String contributor){
        this.prototype = prototype;
        this.region = region;
        this.state = state;
        this.contributor = contributor;
        this.tempPrice = 0;
    }

}
