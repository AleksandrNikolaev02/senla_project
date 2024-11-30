package com.example.demo.repository;

import com.example.demo.enums.ArtifactType;
import com.example.demo.model.Artifact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtifactRepository extends JpaRepository<Artifact, Integer> {
    Page<Artifact> findByArtifactTypeAndCourseId(ArtifactType type, Integer courseId, Pageable pageable);
}
