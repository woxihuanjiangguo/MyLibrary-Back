package fudan.se.lab3.domain.book;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fudan.se.lab3.domain.people.Reader;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("commentId")
    private Long id;

    @JsonProperty("star")
    private int star; //1-5

    @ManyToOne(targetEntity = Reader.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_reader")
    @JsonProperty("reader")
    private Reader reader;

    @JsonProperty("isBad")
    private boolean isBad;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" , timezone = "GMT+8")
    private Date date;

    @JsonProperty("ISBN")
    private String ISBN;
    private String content;

    @OneToMany(targetEntity = Discussion.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "comment_discussion",
            joinColumns = @JoinColumn(name = "commentId"),
            inverseJoinColumns = @JoinColumn(name = "discussionId"))
    @JsonProperty("discussionList")
    private Set<Discussion> discussionList = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public boolean isBad() {
        return isBad;
    }

    public void setBad(boolean bad) {
        isBad = bad;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<Discussion> getDiscussionList() {
        return discussionList;
    }

    public void setDiscussionList(Set<Discussion> discussionList) {
        this.discussionList = discussionList;
    }

    public Reader getReader() {
        return reader;
    }

    public Comment() {
    }

    public Comment(int star, Reader reader, Date date, String ISBN, String content) {
        this.star = star;
        this.reader = reader;
        this.date = date;
        this.ISBN = ISBN;
        this.content = content;
        this.isBad = false;
    }
}
