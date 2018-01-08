package pro.xway.xcloud.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pro.xway.xcloud.services.MyUserService;

@Configuration
@EnableWebSecurity
public class MyConfig extends WebSecurityConfigurerAdapter{
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .antMatchers( "/","/css/*", "/img/**", "/js/*").permitAll()
//                .antMatchers("/panel").hasRole("user")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .permitAll();
    }

    @Autowired
    private MyUserService userService;



    @Bean
    public BCryptPasswordEncoder bcrypt(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
            builder.userDetailsService(userService).passwordEncoder(new PasswordEncoder() {
                @Override
                public String encode(CharSequence charSequence) {
                    return charSequence.toString();
                }

                @Override
                public boolean matches(CharSequence charSequence, String s) {
                    return true;
                }
            });
//        builder.inMemoryAuthentication()
//                .withUser("user").password("password").roles("EDITOR");
    }
}
