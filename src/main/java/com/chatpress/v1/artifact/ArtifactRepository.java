package com.chatpress.v1.artifact;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArtifactRepository extends JpaRepository<Artifact, Long>, JpaSpecificationExecutor<Artifact> {

    Optional<Artifact> findBySlug(String slug);

    Optional<Artifact> findBySlugAndStatus(String slug, Artifact.Status status);
}
