package com.chatpress.artifact;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtifactRepository extends JpaRepository<Artifact, Long>, JpaSpecificationExecutor<Artifact> {

    Optional<Artifact> findBySlug(String slug);

    Optional<Artifact> findBySlugAndStatus(String slug, Artifact.Status status);

    @Query("SELECT a.slug FROM Artifact a WHERE a.slug LIKE CONCAT(:prefix, '%')")
    List<String> findSlugsByPrefix(@Param("prefix") String prefix);

    Page<Artifact> findByStatusOrderByCreatedAtDesc(Artifact.Status status, Pageable pageable);
}
