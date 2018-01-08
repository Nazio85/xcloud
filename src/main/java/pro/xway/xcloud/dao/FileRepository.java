package pro.xway.xcloud.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.xway.xcloud.model.FileXCloud;

import java.util.List;

public interface FileRepository extends JpaRepository<FileXCloud, Long> {
    List<FileXCloud> findByUserIdAndParentId(Long userId, Long parentId);
    List<FileXCloud> findByUserId(Long userId);
    FileXCloud findByNameAndParentId(String name, Long parentId);
}
