package com.example.demo.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RegisterDTO implements Serializable {
    @NotNull(message = "First name cannot be null")
    private String firstName;
    @NotNull(message = "Second name cannot be null")
    private String secondName;
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Password cannot be null")
    @Size(min = 4, message = "Password should have at least 4 characters")
    private String password;
}
