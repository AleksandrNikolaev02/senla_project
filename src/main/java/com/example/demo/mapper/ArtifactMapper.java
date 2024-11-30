package com.example.demo.mapper;

import com.example.demo.dto.SmallArtifactDTO;
import com.example.demo.model.Artifact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArtifactMapper {
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "id", target = "artifactId")
    SmallArtifactDTO artifactToSmallArtifactDTO(Artifact artifact);
}
