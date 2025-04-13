package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.model.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSetting, Integer> {
    UserSetting findByUser(User user);
}
