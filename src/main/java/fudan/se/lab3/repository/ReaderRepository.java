package fudan.se.lab3.repository;

import fudan.se.lab3.domain.people.Attributes;
import fudan.se.lab3.domain.people.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long>{
    Reader findByUsername(String username);
    List<Reader> findAllByAttributes(Attributes attributes);
}

