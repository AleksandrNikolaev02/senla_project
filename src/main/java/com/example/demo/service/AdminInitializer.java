package com.example.demo.service;

import com.example.demo.enums.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                            .fname("Alex")
                            .sname("Admin")
                            .email("admin@mail.ru")
                            .password(passwordEncoder.encode("admin"))
                            .role(Role.ADMIN).build();
            userRepository.save(admin);
            log.info("User with role ADMIN created!");
        }
    }
}

