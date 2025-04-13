package com.example.demo.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserProfileDTO implements Serializable {
    private String fname;
    private String sname;
    private String email;
    private String password;
}
