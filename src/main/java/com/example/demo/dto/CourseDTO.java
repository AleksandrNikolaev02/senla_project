package com.example.demo.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CourseDTO implements Serializable {
    private Integer id;
    private String title;
    private String description;
    private Integer author_id;
    private String fio;
}
