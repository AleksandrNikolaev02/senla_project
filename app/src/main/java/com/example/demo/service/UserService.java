package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.dto.ChangeRoleDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.enums.Role;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.RoleUser;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EntityFindService entityFindService;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, EntityFindService entityFindService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.entityFindService = entityFindService;
    }

    @CacheEvict(value = "users", key = "#username")
    @Loggable(process = "обновление профиля")
    public User updateUserProfile(String username, UserProfileDTO dto) {
        User user = (User) loadUserByUsername(username);

        updateProfileFromUserProfileDto(dto, user);

        saveUserInUserRepository(user);

        return user;
    }

    private void updateProfileFromUserProfileDto(UserProfileDTO dto, User user) {
        Optional.ofNullable(dto.getFname()).ifPresent(user::setFname);
        Optional.ofNullable(dto.getSname()).ifPresent(user::setSname);
        Optional.ofNullable(dto.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(dto.getPassword())
                .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found!"));
    }

    @Loggable(process = "смена роли пользователя")
    public void changeUserRole(ChangeRoleDTO dto) {
        User user = entityFindService.findUserById(dto.getUserId());
        RoleUser newRole = getRoleFromRoleRepository(dto);

        updateRoleUser(newRole, user);
    }

    private void updateRoleUser(RoleUser newRole, User user) {
        user.setRoleUser(newRole);
        saveUserInUserRepository(user);
    }

    private void saveUserInUserRepository(User user) {
        userRepository.save(user);
    }

    private RoleUser getRoleFromRoleRepository(ChangeRoleDTO dto) {
        return roleRepository.findByName(Role.valueOf(dto.getRoleName().toUpperCase()))
                .orElseThrow(() -> new NotFoundException("Role not found!"));
    }
}
