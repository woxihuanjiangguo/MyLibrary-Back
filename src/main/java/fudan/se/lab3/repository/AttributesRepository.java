package fudan.se.lab3.repository;

import fudan.se.lab3.domain.people.Attributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributesRepository extends JpaRepository<Attributes, Long> {
    Attributes findByType(String type);
}
