package fudan.se.lab3.domain.book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class Prototype implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long protoId;

    private String ISBN;
    private String bookName;
    private String author;
    private String intro;
    private String publishTime;
    private double price;

    @JsonProperty("star")
    private double star;
    private int starCount; //记录评分次数

    @OneToMany(targetEntity = Comment.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "prototyoe_comment",
            joinColumns = @JoinColumn(name = "protoId"),
            inverseJoinColumns = @JoinColumn(name = "commentId"))
    @JsonProperty("commentList")
    private Set<Comment> commentList = new LinkedHashSet<>();

    @ManyToOne(targetEntity = PicFile.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "prototype_picfile",
            joinColumns = @JoinColumn(name = "protoId", unique = true),
            inverseJoinColumns = @JoinColumn(name = "fileId"))
    private PicFile picFile;

    public void countStar(int currentRate){ //收到一个新评分时，计算新的总分
        this.starCount++;
        double temp = star;
        this.star = (temp * (starCount - 1) + currentRate) / starCount;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public PicFile getPicFile() {
        return picFile;
    }

    public void setPicFile(PicFile picFile) {
        this.picFile = picFile;
    }

    public Set<Comment> getCommentList() {
        return commentList;
    }

    public Prototype() {
    }

    public Prototype(String ISBN, String bookName, String author, String intro, String publishTime, double price , PicFile picFile) {
        this.ISBN = ISBN;
        this.bookName = bookName;
        this.author = author;
        this.intro = intro;
        this.publishTime = publishTime;
        this.price = price;
        this.picFile = picFile;
        this.starCount = 0;
        this.star = 0;
    }

    public Prototype(Prototype prototype){
        this.ISBN = prototype.getISBN();
        this.bookName = prototype.getBookName();
        this.author = prototype.getAuthor();
        this.intro = prototype.getIntro();
        this.publishTime = prototype.getPublishTime();
        this.price = prototype.getPrice();
        this.picFile = prototype.getPicFile();
        this.starCount = 0;
        this.star = 0;
    }
}
