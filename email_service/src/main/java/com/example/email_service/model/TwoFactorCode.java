package com.example.email_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_codes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwoFactorCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "login", nullable = false, unique = true)
    private String login;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}
