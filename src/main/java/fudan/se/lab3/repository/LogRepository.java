package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.Log;
import fudan.se.lab3.domain.book.PicFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
}
