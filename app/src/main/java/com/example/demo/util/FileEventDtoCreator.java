package com.example.demo.util;

import com.example.demo.exception.FileStorageException;
import com.example.dto.Event;
import com.example.dto.FileEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class FileEventDtoCreator {
    public FileEventDTO create(MultipartFile file, String dir,
                                            String filename, Integer id) {
        try {
            return new FileEventDTO(
                    dir,
                    filename,
                    file.getContentType(),
                    file.getBytes(),
                    id,
                    Event.ANSWER
            );
        } catch (IOException e) {
            log.error("Ошибка чтения байтов файла {}", filename);
            throw new FileStorageException("File read error!");
        }
    }
}
