package com.example.demo.dto;

import java.io.Serializable;

import com.example.demo.enums.ArtifactType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SmallArtifactDTO implements Serializable {
    private Integer artifactId;
    private Integer courseId;
    private String content;
    private String title;
    private ArtifactType artifactType;
}
