package com.marindulja.mentalhealthbackend;

import com.marindulja.mentalhealthbackend.models.Gender;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.SUPERADMIN).isEmpty()) {
                User superAdmin = new User();
                UserProfile superAdminUserProfile = new UserProfile();
                superAdmin.setUsername("marindulja");
                superAdmin.setPassword(passwordEncoder.encode("Superadmin19!"));
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setEmail("duljamarin@gmail.com");
                superAdminUserProfile.setGender(Gender.MALE);
                superAdminUserProfile.setPhoneNumber("+355684448934");
                superAdminUserProfile.setUser(superAdmin); // Set the user to the profile
                superAdmin.setUserProfile(superAdminUserProfile); // Set the profile to the user
                userRepository.save(superAdmin);
            }
        };
    }

}
