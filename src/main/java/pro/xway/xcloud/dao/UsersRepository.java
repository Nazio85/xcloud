package pro.xway.xcloud.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.xway.xcloud.model.UserXCloud;

//@Repository
public interface UsersRepository extends JpaRepository<UserXCloud, Long> {
    UserXCloud findByUsername(String name);
}
