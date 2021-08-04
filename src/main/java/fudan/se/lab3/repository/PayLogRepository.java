package fudan.se.lab3.repository;

import fudan.se.lab3.domain.people.PayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayLogRepository extends JpaRepository<PayLog, Integer> {
}
