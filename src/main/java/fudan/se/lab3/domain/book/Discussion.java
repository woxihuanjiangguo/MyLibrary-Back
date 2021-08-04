package fudan.se.lab3.domain.book;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fudan.se.lab3.domain.people.Reader;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Discussion implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("discussionId")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" , timezone = "GMT+8")
    private Date date;

    @ManyToOne(targetEntity = Reader.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "discussion_reader")
    @JsonProperty("reader")
    private Reader reader;

    @ManyToOne(targetEntity = Comment.class , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JoinTable(name = "discussion_comment",
            joinColumns = @JoinColumn(name = "discussionId"),
            inverseJoinColumns = @JoinColumn(name = "commentId"))
    @JsonIgnore
    private Comment comment;

    private String reply;
    private String content;

    @JsonProperty("isBad")
    private boolean isBad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isBad() {
        return isBad;
    }

    public void setBad(boolean bad) {
        isBad = bad;
    }

    public Comment getComment() {
        return comment;
    }

    public Discussion() {
    }

    public Discussion(Date date, Reader reader, String reply, String content, Comment comment) {
        this.date = date;
        this.reader = reader;
        this.reply = reply;
        this.content = content;
        this.isBad = false;
        this.comment = comment;
    }
}
