package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.Copy;
import fudan.se.lab3.domain.book.Prototype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CopyRepository extends JpaRepository<Copy, Integer> {
    Copy findByCid(String cid);
    List<Copy> findByReserverId(String id);
    List<Copy> findByPrototype(Prototype prototype);
    List<Copy> findByBorrowerId(String id);
    List<Copy> findByTag(String usernameFromToken);
}
