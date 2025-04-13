package com.example.demo.service;

import com.example.demo.enums.Role;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.RoleUser;
import com.example.demo.model.User;
import com.example.demo.model.UserSetting;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSettingRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSettingRepository userSettingRepository;

    @PostConstruct
    public void initAdmin() {
        if (isUserRepositoryEmpty()) {
            User admin = createAdminUser();
            createUserSettings(admin);
            log.info("User with role ADMIN created!");
        }
    }

    private boolean isUserRepositoryEmpty() {
        return userRepository.count() == 0;
    }

    private User createAdminUser() {
        RoleUser role = getAdminRole();
        User admin = buildAdminUser(role);
        return userRepository.save(admin);
    }

    private RoleUser getAdminRole() {
        return roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new NotFoundException("Role not found!"));
    }

    private User buildAdminUser(RoleUser role) {
        return User.builder()
                .fname("Alex")
                .sname("Admin")
                .email("admin@mail.ru")
                .password(passwordEncoder.encode("admin"))
                .roleUser(role)
                .build();
    }

    private void createUserSettings(User admin) {
        UserSetting setting = UserSetting.builder()
                .user(admin)
                .twoFactor(false)
                .build();

        userSettingRepository.save(setting);
    }
}

