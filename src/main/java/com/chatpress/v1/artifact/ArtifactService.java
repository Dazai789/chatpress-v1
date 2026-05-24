package com.chatpress.v1.artifact;

import com.chatpress.v1.artifact.exception.DuplicateSlugException;
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
        return artifactRepository.findAll();
    }

    public Optional<Artifact> getArtifact(Long id) {
        return artifactRepository.findById(id);
    }

    public Optional<Artifact> getArtifactBySlug(String slug) {
        return artifactRepository.findBySlug(slug);
    }

    public Optional<Artifact> updateArtifact(Long id, String title, String slug, String sourceContent) {
        return artifactRepository.findById(id)
                .map(artifact -> {
                    ensureSlugAvailableForUpdate(slug, artifact.getId());
                    artifact.setTitle(title);
                    artifact.setSlug(slug);
                    artifact.setSourceContent(sourceContent);
                    artifact.setRenderedHtml(markdownRenderer.render(sourceContent));
                    return artifactRepository.save(artifact);
                });
    }

    public boolean deleteArtifact(Long id) {
        if (!artifactRepository.existsById(id)) {
            return false;
        }

        artifactRepository.deleteById(id);
        return true;
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
