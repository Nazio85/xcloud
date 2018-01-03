package pro.xway.xcloud.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.xway.xcloud.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
}
