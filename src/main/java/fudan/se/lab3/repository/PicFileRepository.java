package fudan.se.lab3.repository;

import fudan.se.lab3.domain.book.PicFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author LBW
 */
@Repository
public interface PicFileRepository extends JpaRepository<PicFile, Integer> {
    PicFile findByFileId(int fileId);
}
