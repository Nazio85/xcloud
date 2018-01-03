package pro.xway.xcloud.model;


import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserXCloud {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
    private String password;
    private String username;

    protected UserXCloud() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public UserXCloud(Set<Role> roles, String password, String username) {
        this.roles = roles;
        this.password = password;
        this.username = username;
    }
}
