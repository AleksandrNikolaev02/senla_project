package com.example.demo.repository;

import com.example.demo.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    Page<Message> findBySenderIdAndRecipientId(Integer senderId, Integer recipientId,
                                               Pageable pageable);
    @Query("SELECT m FROM Message m WHERE (m.recipient.id = :recipientId AND m.sender.id = :senderId)" +
            "OR (m.recipient.id = :senderId AND m.sender.id = :recipientId)")
    Page<Message> getCorrespondence(@Param("senderId") Integer senderId, @Param("recipientId") Integer recipientId,
                                    Pageable pageable);
}
