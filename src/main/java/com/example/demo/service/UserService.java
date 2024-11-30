package com.example.demo.service;

import com.example.demo.dto.ChangeRoleDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.enums.Role;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.JwtAuthenticationResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
                       @Lazy PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @CacheEvict(value = "users", allEntries = true)
    public JwtAuthenticationResponse signUp(RegisterDTO registerDTO) {
        User user = User.builder()
                .fname(registerDTO.getFirst_name())
                .sname(registerDTO.getSecond_name())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        log.info("Процесс: регистрация пользователя. Польлователь {} успешно зарегистрировался!", user);

        String jwt = jwtTokenProvider.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    @CacheEvict(value = "users", allEntries = true)
    public JwtAuthenticationResponse signIn(LoginDTO loginDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getPassword()
        ));

        UserDetails user = loadUserByUsername(loginDTO.getEmail());

        String jwt = jwtTokenProvider.generateToken(user);

        log.info("Процесс: аутентификация пользователя. Польлователь {} успешно аутентифицировался!", user);

        return new JwtAuthenticationResponse(jwt);
    }

    @CacheEvict(value = "users", key = "#username")
    public User updateProfile(String username, UserProfileDTO dto) {
        User user = (User) loadUserByUsername(username);

        if (dto.getFname() != null) {
            user.setFname(dto.getFname());
        }
        if (dto.getSname() != null) {
            user.setSname(dto.getSname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);

        log.info("Процесс: обновление профиля. Профиль пользователя {} успешно обновлен!", user);

        return user;
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User not found!"));
    }

    public void changeUserRole(ChangeRoleDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setRole(Role.valueOf(dto.getRoleName().toUpperCase()));
        userRepository.save(user);

        log.info("Процесс: смена роли пользователя. Роль пользователя {} успешно сменена!", user);
    }
}
