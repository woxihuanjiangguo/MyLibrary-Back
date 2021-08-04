package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.Discussion;
import fudan.se.lab3.domain.people.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    Discussion findDiscussionById(Long id);
    List<Discussion> findDiscussionsByReader(Reader reader);
}
