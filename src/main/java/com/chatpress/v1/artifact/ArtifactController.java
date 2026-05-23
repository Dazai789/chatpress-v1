package com.chatpress.v1.artifact;

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
                request.sourceContent()
        );
        return ArtifactResponse.from(artifact);
    }

    @GetMapping
    public List<ArtifactResponse> listArtifacts() {
        return artifactService.listArtifacts().stream()
                .map(ArtifactResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtifactResponse> getArtifact(@PathVariable Long id) {
        return artifactService.getArtifact(id)
                .map(ArtifactResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtifactResponse> updateArtifact(
            @PathVariable Long id,
            @Valid @RequestBody ArtifactRequest request
    ) {
        return artifactService.updateArtifact(
                        id,
                        request.title(),
                        request.slug(),
                        request.sourceContent()
                )
                .map(ArtifactResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtifact(@PathVariable Long id) {
        if (!artifactService.deleteArtifact(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
