package com.example.demo.util;

import com.example.demo.exception.FileStorageException;
import org.springframework.stereotype.Component;

@Component
public class FileNameValidator {
    public void validate(String filename) {
        if (filename == null || filename.contains("..")) {
            throw new FileStorageException("Invalid file path sequence: " + filename);
        }
    }
}
