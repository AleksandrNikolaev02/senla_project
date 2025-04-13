package com.example.email_service.repository;

import com.example.email_service.model.TwoFactorCode;
import io.micrometer.core.annotation.Timed;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Integer> {
    @Timed(value = "find_by_login_time_metric")
    List<TwoFactorCode> findByLogin(String login);
    @Modifying
    @Transactional
    @Query("DELETE FROM TwoFactorCode t WHERE t.expiredAt < :now")
    @Timed(value = "delete_all_expired_codes_time_metric")
    int deleteAllExpiredCodes(@Param("now") LocalDateTime now);
}
