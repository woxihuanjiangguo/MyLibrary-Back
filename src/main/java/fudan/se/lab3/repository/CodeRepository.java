package fudan.se.lab3.repository;

import fudan.se.lab3.domain.people.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {
    Code findByEmail(String email);
    boolean existsAllByEmail(String email);
    void deleteAllByEmail(String email);
}
