package com.example.demo.repository;

import com.example.demo.enums.Role;
import com.example.demo.model.RoleUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<RoleUser, Integer> {
    Optional<RoleUser> findByName(Role role);
}
