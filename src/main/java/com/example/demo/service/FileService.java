package com.example.demo.service;

import com.example.demo.config.FileConfig;
import com.example.demo.exception.FileStorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Getter
@AllArgsConstructor
public class FileService {
    private final FileConfig fileConfig;
    private final MinioClient minioClient;

    @SneakyThrows
    public String storeFile(String dir, MultipartFile file) {
        log.info("Процесс: создание директории. Директория: {}", fileConfig.getDir());
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(fileConfig.getDir()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(fileConfig.getDir()).build());
        }
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(fileConfig.getDir())
                .object(dir + "/" + file.getOriginalFilename())
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();

        ObjectWriteResponse response = minioClient.putObject(args);

        log.info("Процесс: сохранение файла. Директория: {}", response.bucket() + "/" + response.object());

        return response.object();
    }

    public Resource getFile(String path) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(fileConfig.getDir())
                    .object(path)
                    .build();
            InputStream inputStream = minioClient.getObject(args);

            if (inputStream == null) {
                log.error("Файл не найден в MinIO: {}", path);
                throw new FileStorageException("Файл не найден!");
            }

            log.info("Процесс: получение файла из Minio. Файл успешно получен из MinIO: {}", path);

            byte[] fileData = inputStream.readAllBytes();
            return new ByteArrayResource(fileData);
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                XmlParserException e) {
            log.error("Ошибка при получении файла из MinIO: {}", e.getMessage());
            throw new FileStorageException("Ошибка при получении файла!");
        }
    }

    @SneakyThrows
    public void deleteDirectory(String path) {
        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(fileConfig.getDir())
                        .prefix(path)
                        .recursive(true)
                        .build()
        );

        List<DeleteObject> objectsToDelete = new ArrayList<>();
        for (Result<Item> item : items) {
            objectsToDelete.add(new DeleteObject(item.get().objectName()));
        }

        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(fileConfig.getDir())
                        .objects(objectsToDelete)
                        .build()
        );
    }
}
