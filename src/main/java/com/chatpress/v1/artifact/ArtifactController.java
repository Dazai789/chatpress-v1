package com.chatpress.v1.artifact;

import com.chatpress.v1.artifact.dto.ArtifactRequest;
import com.chatpress.v1.artifact.dto.ArtifactResponse;
import com.chatpress.v1.artifact.dto.ArtifactStatusRequest;
import com.chatpress.v1.artifact.dto.ArtifactSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtifactResponse createArtifact(@Valid @RequestBody ArtifactRequest request) {
        Artifact artifact = artifactService.createArtifact(
                request.title(),
                request.slug(),
                request.toArtifactSourceType(),
                request.sourceContent()
        );
        return ArtifactResponse.from(artifact);
    }

    @GetMapping
    public List<ArtifactSummaryResponse> listArtifacts() {
        return artifactService.listArtifacts().stream()
                .map(ArtifactSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ArtifactResponse getArtifact(@PathVariable Long id) {
        return ArtifactResponse.from(artifactService.getArtifactOrThrow(id));
    }

    @PutMapping("/{id}")
    public ArtifactResponse updateArtifact(
            @PathVariable Long id,
            @Valid @RequestBody ArtifactRequest request
    ) {
        Artifact artifact = artifactService.updateArtifactOrThrow(
                id,
                request.title(),
                request.slug(),
                request.toArtifactSourceType(),
                request.sourceContent()
        );
        return ArtifactResponse.from(artifact);
    }

    @PutMapping("/{id}/status")
    public ArtifactResponse updateArtifactStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArtifactStatusRequest request
    ) {
        Artifact artifact = artifactService.updateArtifactStatusOrThrow(id, request.toArtifactStatus());
        return ArtifactResponse.from(artifact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtifact(@PathVariable Long id) {
        artifactService.deleteArtifactOrThrow(id);
        return ResponseEntity.noContent().build();
    }
}
