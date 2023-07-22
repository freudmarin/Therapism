package com.marindulja.mentalhealthbackend;

import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MentalHealthBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentalHealthBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.SUPERADMIN).isEmpty()) {
                User superAdmin = new User();
                superAdmin.setUsername("marindulja");
                superAdmin.setPassword(passwordEncoder.encode("Superadmin19!"));
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setEmail("duljamarin@gmail.com");
                superAdmin.setInstitution(null);
                superAdmin.setTherapist(null);
                userRepository.save(superAdmin);
            }
        };
    }

}
