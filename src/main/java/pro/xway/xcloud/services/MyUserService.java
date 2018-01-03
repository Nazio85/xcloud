package pro.xway.xcloud.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.xway.xcloud.dao.*;
import pro.xway.xcloud.model.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Service
public class MyUserService implements UserDetailsService {
    public static final String ADMIN = "admin";
    public static final String USER = "user";
    @Autowired
    private UsersRepository userDao;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional (readOnly = true)
    public UserDetails loadUserByUsername(@NotNull String name) throws UsernameNotFoundException {
        UserXCloud user = userDao.findByUsername(name);

        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();

        for (Role authority : user.getRoles()) {
            grantedAuthoritySet.add(new SimpleGrantedAuthority(authority.getName()));
        }

        User userSecurity = new User(user.getUsername(), user.getPassword(), grantedAuthoritySet);
        return userSecurity;
    }

    @PostConstruct
    public void init() {
        //Первичный старт, создание админа
        //При первом входе необходимло изменить пароль
        Role role = roleRepository.findByName(ADMIN);
        if (role == null) {
            role = new Role(ADMIN);
            roleRepository.save(role);
            role = new Role(USER);
            roleRepository.save(role);
        }

        UserXCloud user = userDao.findByUsername(USER);

        if (user == null) {
            HashSet<Role> roles = new HashSet<>();
            roles.add(role);
            String pas = passwordEncoder.encode("123");
            user = new UserXCloud(roles, "123", USER);
            userDao.save(user);
        }


//        if (user !=  null) {
//            User user1 = userDao.findByUsername(ADMIN);
//            userDao.delete(user1);
//        }
    }
}
