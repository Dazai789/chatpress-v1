package com.chatpress.v1.artifact;

import com.chatpress.v1.artifact.exception.ArtifactNotFoundException;
import com.chatpress.v1.artifact.exception.DuplicateSlugException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final MarkdownRenderer markdownRenderer;

    public ArtifactService(ArtifactRepository artifactRepository, MarkdownRenderer markdownRenderer) {
        this.artifactRepository = artifactRepository;
        this.markdownRenderer = markdownRenderer;
    }

    public Artifact createArtifact(String title, String slug, String sourceContent) {
        ensureSlugAvailableForCreate(slug);

        Artifact artifact = new Artifact(title, slug, sourceContent, markdownRenderer.render(sourceContent));
        artifact.setStatus(Artifact.Status.PUBLISHED);
        return artifactRepository.save(artifact);
    }

    public List<Artifact> listArtifacts() {
        return artifactRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Artifact getArtifactOrThrow(Long id) {
        return artifactRepository.findById(id)
                .orElseThrow(() -> new ArtifactNotFoundException(id));
    }

    public Optional<Artifact> getArtifactBySlug(String slug) {
        return artifactRepository.findBySlug(slug);
    }

    public Artifact updateArtifactOrThrow(Long id, String title, String slug, String sourceContent) {
        Artifact artifact = getArtifactOrThrow(id);
        ensureSlugAvailableForUpdate(slug, artifact.getId());
        artifact.setTitle(title);
        artifact.setSlug(slug);
        artifact.setSourceContent(sourceContent);
        artifact.setRenderedHtml(markdownRenderer.render(sourceContent));
        return artifactRepository.save(artifact);
    }

    public void deleteArtifactOrThrow(Long id) {
        getArtifactOrThrow(id);
        artifactRepository.deleteById(id);
    }

    private void ensureSlugAvailableForCreate(String slug) {
        if (artifactRepository.findBySlug(slug).isPresent()) {
            throw new DuplicateSlugException(slug);
        }
    }

    private void ensureSlugAvailableForUpdate(String slug, Long currentArtifactId) {
        artifactRepository.findBySlug(slug)
                .filter(existingArtifact -> !existingArtifact.getId().equals(currentArtifactId))
                .ifPresent(existingArtifact -> {
                    throw new DuplicateSlugException(slug);
                });
    }
}
