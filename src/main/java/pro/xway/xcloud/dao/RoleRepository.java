package pro.xway.xcloud.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.xway.xcloud.model.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
