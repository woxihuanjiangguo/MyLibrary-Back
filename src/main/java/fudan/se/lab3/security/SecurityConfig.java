package fudan.se.lab3.security;

import fudan.se.lab3.security.jwt.JwtRequestFilter;
import fudan.se.lab3.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author LBW
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwtUserDetailsService userDetailsService;
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(JwtUserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/welcome2")
                .permitAll()
                //登录与注册开放所有权限
                .antMatchers("/login")
                .permitAll()
                .antMatchers("/register")
                .permitAll()
                .antMatchers("/sendEmail")
                .permitAll()
                // 仅限于reader的一些接口
                .antMatchers("/reader/**")
                .hasAnyAuthority("Reader","SuperLibrarian")
                //books学生与图书管理员均可，system只有图书管理员可访问
                .antMatchers("/books/search/**")
                .permitAll()
                .antMatchers("/system/**")
                .hasAnyAuthority("Librarian","SuperLibrarian")
                .antMatchers("/system/add","/system/checkOverdue","/system/alterAttributes")
                .hasAnyAuthority("SuperLibrarian")
                //后续会更改为管理员才可上传
                /*.antMatchers("/**")
                .permitAll()*/
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();
        //      Here we use JWT(Json Web Token) to authenticate the user.
//      You need to write your code in the class 'JwtRequestFilter' to make it works.
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Hint: Now you can view h2-console page at `http://IP-Address:<port>/h2-console` without authentication.
        web.ignoring().antMatchers("/h2-console/**");
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
