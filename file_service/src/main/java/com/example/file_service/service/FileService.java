package com.example.file_service.service;

import com.example.dto.Event;
import com.example.dto.FileDataDTO;
import com.example.dto.FileEventDTO;
import com.example.dto.FileResponseDTO;
import com.example.dto.Status;
import com.example.file_service.config.FileConfig;
import com.example.file_service.exception.FileStorageException;
import com.example.file_service.interfaces.Mapper;
import com.example.file_service.metric.CustomMetricService;
import com.example.file_service.util.EventToTopicsStorage;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class FileService {
    private final FileConfig fileConfig;
    private final MinioClient minioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventToTopicsStorage eventToTopicsStorage;
    private final CustomMetricService customMetricService;
    @Qualifier(value = "jsonMapper")
    private final Mapper mapper;

    @KafkaListener(topics = "${topics.file-events}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   errorHandler = "customKafkaErrorHandler")
    @SneakyThrows
    public void storeFile(String event) {
        FileEventDTO fileEventDTO = (FileEventDTO) mapper.deserialize(event, FileEventDTO.class);

        log.info("Процесс: создание директории. Директория: {}", fileConfig.getBucket());

        if (!isBucketExists(fileConfig.getBucket())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(fileConfig.getBucket()).build());
        }

        PutObjectArgs args = createPutObjectArgs(fileEventDTO);
        ObjectWriteResponse response = saveFileInMinio(args);

        FileResponseDTO fileResponseDTO = createFileResponseDTO(fileEventDTO, response);

        String topic = definingTopicRelativeEvent(fileEventDTO.getEvent());

        customMetricService.incrementSuccessMinioServiceMetric();

        kafkaTemplate.send(topic, mapper.serialize(fileResponseDTO));
    }

    @SneakyThrows
    private boolean isBucketExists(String bucket) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    }

    private PutObjectArgs createPutObjectArgs(FileEventDTO dto) {
        return PutObjectArgs.builder()
                .bucket(fileConfig.getBucket())
                .object(dto.getDir() + "/" + dto.getFilename())
                .stream(new ByteArrayInputStream(dto.getFileData()), dto.getFileData().length, -1)
                .contentType(dto.getContentType())
                .build();
    }

    @SneakyThrows
    private ObjectWriteResponse saveFileInMinio(PutObjectArgs args) {
        ObjectWriteResponse response = minioClient.putObject(args);

        log.info("Процесс: сохранение файла. Директория: {}", response.bucket() + "/" + response.object());

        return response;
    }

    private FileResponseDTO createFileResponseDTO(FileEventDTO dto, ObjectWriteResponse response) {
        return new FileResponseDTO(
                dto.getFilename(),
                response.object(),
                dto.getArtifactId()
        );
    }

    private String definingTopicRelativeEvent(Event event) {
        return eventToTopicsStorage.getTopicByEvent(event);
    }

    @KafkaListener(topics = "${topics.get-file-request}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   errorHandler = "customKafkaErrorHandler")
    @SendTo("#{topicConfig.getGetFileReply()}")
    @SneakyThrows
    public String getFile(String path) {
        byte[] fileData = getFileFromMinio(fileConfig.getBucket(), path);
        FileDataDTO dto = createFileDataDTO(fileData);

        customMetricService.incrementSuccessMinioServiceMetric();

        return mapper.serialize(dto);
    }

    private byte[] getFileFromMinio(String bucket, String path) throws Exception {
        GetObjectArgs args = createGetObjectsArgsByBucketAndPath(
                bucket,
                path);
        InputStream inputStream = minioClient.getObject(args);

        if (inputStream == null) {
            throw new FileStorageException(String.format("Файл не найден в MinIO по пути: %s", path));
        }

        log.info("Процесс: получение файла из Minio. Файл успешно получен из MinIO: {}", path);

        return inputStream.readAllBytes();
    }

    private GetObjectArgs createGetObjectsArgsByBucketAndPath(String bucket, String path) {
        return GetObjectArgs.builder()
                .bucket(bucket)
                .object(path)
                .build();
    }

    private FileDataDTO createFileDataDTO(byte[] fileData) {
        return new FileDataDTO(fileData, Status.OK);
    }

    @KafkaListener(topics = "${topics.delete-file-request}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   errorHandler = "customKafkaErrorHandler")
    @SneakyThrows
    public void deleteDirectory(String path) {
        log.info("Процесс удаления дериктории {}: начало процесса удаления директории.", path);

        List<DeleteObject> objectsToDelete = createListObjectsToDelete(fileConfig.getBucket(), path);

        removeAllObjectsToDelete(fileConfig.getBucket(), objectsToDelete);

        customMetricService.incrementSuccessMinioServiceMetric();

        log.info("Процесс удаления дериктории {}: удаление директории завершено.", path);
    }

    private List<DeleteObject> createListObjectsToDelete(String bucket, String path) throws Exception {
        Iterable<Result<Item>> items = minioClient.listObjects(
                createListObjectsArgs(bucket, path)
        );

        List<DeleteObject> objectsToDelete = new ArrayList<>();
        for (Result<Item> item : items) {
            objectsToDelete.add(new DeleteObject(item.get().objectName()));
        }

        return objectsToDelete;
    }

    private ListObjectsArgs createListObjectsArgs(String bucket, String path) {
        return ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(path)
                .recursive(true)
                .build();
    }

    private void removeAllObjectsToDelete(String bucket, List<DeleteObject> objectsToDelete) {
        minioClient.removeObjects(
                createRemoveObjectsArgs(bucket, objectsToDelete)
        );
    }

    private RemoveObjectsArgs createRemoveObjectsArgs(String bucket, List<DeleteObject> objectsToDelete) {
        return RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(objectsToDelete)
                .build();
    }
}
