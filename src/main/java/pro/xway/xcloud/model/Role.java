package pro.xway.xcloud.model;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @ManyToMany(mappedBy = "roles")
    private Set<UserXCloud> users;

    protected Role() {
    }

    @Override
    public String getAuthority() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<UserXCloud> getUsers() {
        return users;
    }

    public void setUsers(Set<UserXCloud> users) {
        this.users = users;
    }

    public Role(String name) {
        this.name = name;
        this.users = new HashSet<>();
    }
}
