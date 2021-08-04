package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.Comment;
import fudan.se.lab3.domain.book.Prototype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrototypeRepository extends JpaRepository<Prototype,Integer> {
    Prototype findByISBN(String ISBN);
    List<Prototype> findByBookNameLike(String bookName);
    List<Prototype> findByAuthorLike(String author);
}
