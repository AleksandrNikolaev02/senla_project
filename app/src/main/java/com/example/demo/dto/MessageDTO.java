package com.example.demo.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageDTO implements Serializable {
    @NotNull(message = "Content cannot be null")
    private String content;
    private Integer senderId;
    @NotNull(message = "Recipient ID cannot be null")
    @Min(value = 1, message = "Recipient ID must be greater than 0")
    private Integer recipientId;
}
