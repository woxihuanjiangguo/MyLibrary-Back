package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.Comment;
import fudan.se.lab3.domain.people.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Comment findCommentById(Long id);
    List<Comment> findCommentsByReader(Reader reader);
    List<Comment> findCommentsByISBN(String ISBN);
}
