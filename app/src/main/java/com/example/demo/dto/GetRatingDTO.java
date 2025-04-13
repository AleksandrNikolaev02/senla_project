package com.example.demo.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetRatingDTO implements Serializable {
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Integer courseId;
}
