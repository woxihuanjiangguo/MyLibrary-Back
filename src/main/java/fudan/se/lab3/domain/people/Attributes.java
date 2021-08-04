package fudan.se.lab3.domain.people;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Attributes {

    @Id
    @JsonProperty("type")
    private String type;

    private int maxBorrow;
    private String reserveDuration;
    private String borrowDuration;


    public Attributes(){

    }

    public Attributes(String type, int maxBorrow, String reserveDuration, String borrowDuration) {
        this.type = type;
        this.maxBorrow = maxBorrow;
        this.reserveDuration = reserveDuration;
        this.borrowDuration = borrowDuration;
    }

    public int getMaxBorrow() {
        return maxBorrow;
    }

    public String getReserveDuration() {
        return reserveDuration;
    }

    public String getBorrowDuration() {
        return borrowDuration;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMaxBorrow(int maxBorrow) {
        this.maxBorrow = maxBorrow;
    }

    public void setReserveDuration(String reserveDuration) {
        this.reserveDuration = reserveDuration;
    }

    public void setBorrowDuration(String borrowDuration) {
        this.borrowDuration = borrowDuration;
    }
}
