package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileEventDTO implements Serializable {
    private String dir;
    private String filename;
    private String contentType;
    @ToString.Exclude
    private byte[] fileData;
    private Integer artifactId;
    private Event event;
}