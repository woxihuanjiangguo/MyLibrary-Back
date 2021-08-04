package fudan.se.lab3.repository;

import fudan.se.lab3.domain.people.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
