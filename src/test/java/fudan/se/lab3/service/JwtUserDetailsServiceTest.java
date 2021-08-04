package fudan.se.lab3.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class JwtUserDetailsServiceTest {
    @Autowired
    private JwtUserDetailsService service;

    @Test
    public void noSuchUser() {
        try {
            service.loadUserByUsername("gulu");
        }catch (UsernameNotFoundException ex){
            assertEquals("gulu",ex.getMessage());
        }
    }

    @Test
    public void hasSuchUser() {
        try {
            service.loadUserByUsername("admin");
        }catch (UsernameNotFoundException ex){
            assertEquals("admin",ex.getMessage());
        }

    }
}