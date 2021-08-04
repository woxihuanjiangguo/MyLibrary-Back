package fudan.se.lab3;

import fudan.se.lab3.domain.people.Attributes;
import fudan.se.lab3.domain.people.Authority;
import fudan.se.lab3.domain.people.User;
import fudan.se.lab3.repository.AttributesRepository;
import fudan.se.lab3.repository.AuthorityRepository;
import fudan.se.lab3.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

/**
 * Group No. 17 ---- Backend Entrance
 */
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(UserRepository userRepository, AuthorityRepository authorityRepository, AttributesRepository attributesRepository, PasswordEncoder passwordEncoder) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                // Create authorities if not exist.
                Authority librarianAuthority = getOrCreateAuthority("Librarian", authorityRepository);
                Authority readerAuthority = getOrCreateAuthority("Reader", authorityRepository);
                Authority superLibrarianAuthority = getOrCreateAuthority("SuperLibrarian",authorityRepository);

                // Create attributes for each identity
                Attributes graduateAttr = getOrCreateAttributes("Undergraduate", 5 , "3,0,0,0", "7,0,0,0", attributesRepository);
                Attributes postgraduateAttr = getOrCreateAttributes("Postgraduate", 6 , "4,0,0,0", "8,0,0,0", attributesRepository);
                Attributes teacherAttr = getOrCreateAttributes("Teacher", 10 , "5,0,0,0" , "15,0,0,0", attributesRepository);

                // Create a SuperLibrarian
                if (userRepository.findByUsername("admin") == null) {
                    User admin = new User(
                            "admin",
                            passwordEncoder.encode("password"),
                            new HashSet<>(Collections.singletonList(superLibrarianAuthority))
                    );
                    userRepository.save(admin);
                }
            }

            private Authority getOrCreateAuthority(String authorityText, AuthorityRepository authorityRepository) {
                Authority authority = authorityRepository.findByAuthority(authorityText);
                if (authority == null) {
                    authority = new Authority(authorityText);
                    authorityRepository.save(authority);
                }
                return authority;
            }

            private Attributes getOrCreateAttributes(String type,int maxBorrow,String reserveDuration, String borrowDuration, AttributesRepository attributesRepository) {
                Attributes attributes = attributesRepository.findByType(type);
                if (attributes == null) {
                    attributes = new Attributes(type,maxBorrow,reserveDuration,borrowDuration);
                    attributesRepository.save(attributes);
                }
                return attributes;
            }
        };
    }
}

