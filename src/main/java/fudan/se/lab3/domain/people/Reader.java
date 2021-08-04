package fudan.se.lab3.domain.people;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fudan.se.lab3.domain.book.Log;
import fudan.se.lab3.domain.book.Prototype;

import javax.persistence.*;
import javax.validation.Payload;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Reader extends User {

    private String email;
    private int currentNumber;          // record the number of books reserved and borrowed
    private int score;                          // record the credit

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonProperty("attributes")
    private Attributes attributes;

    @OneToMany(targetEntity = PayLog.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "reader_paylog",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "payLogId"))
    List<PayLog> payLogList = new ArrayList<>();

    @OneToMany(targetEntity = Prototype.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "reader_prototype",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "idprototypeId"))
    @JsonIgnore
    Set<Prototype> notCommentedPrototypeList = new LinkedHashSet<>();

    public Reader(){

    }

    public Reader(String username,String password,String email, Set<Authority> authorities,Attributes attributes){
        super(username,password,authorities);
        this.email = email;
        this.attributes = attributes;
        this.currentNumber = 0;
        this.score = 100;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void resetScore(){
        this.score = 100;
    }

    public void decrementScore(int decrement){
        this.score -= decrement;
    }

    // currentNumber = borrowNum + reserveNum <= a.getMaxBorrow
    public int getCurrentNumber(){
        return  this.currentNumber;
    }

    public int getMaxBorrow() {
        return attributes.getMaxBorrow();
    }

    public String getReserveDuration() {
        return attributes.getReserveDuration();
    }

    public List<PayLog> getPayLogList() {
        return payLogList;
    }

    public String getBorrowDuration() {
        return attributes.getBorrowDuration();    }

    public void addCurrentNumber() {
        this.currentNumber++;
    }

    public void decrementCurrentNumber(){
        if(this.currentNumber > 0){
            this.currentNumber --;
        }
    }

    public void addPayLog(PayLog payLog){
        this.payLogList.add(payLog);
    }

    public void addNotCommentedList(Prototype prototype){
        this.notCommentedPrototypeList.add(prototype);
    }

    public Set<Prototype> getNotCommentedPrototypeList() {
        return notCommentedPrototypeList;
    }
}
