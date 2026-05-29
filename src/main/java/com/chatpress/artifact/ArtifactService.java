package com.chatpress.artifact;

import com.chatpress.artifact.exception.ArtifactNotFoundException;
import com.chatpress.artifact.exception.InvalidArtifactQueryException;
import com.chatpress.artifact.exception.InvalidMarkdownImportException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class ArtifactService {

    private static final long MAX_MARKDOWN_FILE_SIZE = 2 * 1024 * 1024;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    private final ArtifactRepository artifactRepository;
    private final MarkdownRenderer markdownRenderer;

    public ArtifactService(ArtifactRepository artifactRepository, MarkdownRenderer markdownRenderer) {
        this.artifactRepository = artifactRepository;
        this.markdownRenderer = markdownRenderer;
    }

    @Transactional
    public Artifact createArtifact(String title, String sourceContent, String username) {
        String finalSlug = generateSlug(title);

        Artifact artifact = new Artifact(title, finalSlug, sourceContent, markdownRenderer.render(sourceContent), username);
        artifact.setStatus(Artifact.Status.PUBLISHED);
        return artifactRepository.save(artifact);
    }

    @Transactional
    public Artifact importMarkdownFile(MultipartFile file, String title, String username) {
        validateMarkdownFile(file);

        String finalTitle = normalizeTitle(title)
                .orElseGet(() -> titleFromFilename(file.getOriginalFilename()));
        validateTitle(finalTitle);

        String sourceContent = readMarkdownContent(file);
        if (sourceContent.isBlank()) {
            throw new InvalidMarkdownImportException("Markdown file must not be empty");
        }

        return createArtifact(finalTitle, sourceContent, username);
    }

    public Page<Artifact> listArtifacts(int page, int size, String q, String status, String username) {
        PageRequest pageRequest = PageRequest.of(
                normalizePage(page),
                normalizePageSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Artifact> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("createdBy"), username);

        Optional<String> keyword = normalizeSearchKeyword(q);
        if (keyword.isPresent()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + keyword.get().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        Optional<Artifact.Status> artifactStatus = parseStatus(status);
        if (artifactStatus.isPresent()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), artifactStatus.get())
            );
        }

        return artifactRepository.findAll(specification, pageRequest);
    }

    public Artifact getArtifactOrThrow(Long id, String username) {
        Artifact artifact = artifactRepository.findById(id)
                .orElseThrow(() -> new ArtifactNotFoundException(id));
        if (!artifact.getCreatedBy().equals(username)) {
            throw new ArtifactNotFoundException(id);
        }
        return artifact;
    }

    public Optional<Artifact> getArtifactBySlug(String slug) {
        return artifactRepository.findBySlug(slug);
    }

    public Optional<Artifact> getPublishedArtifactBySlug(String slug) {
        return artifactRepository.findBySlugAndStatus(slug, Artifact.Status.PUBLISHED);
    }

    @Transactional
    public Artifact updateArtifactOrThrow(Long id, String title, String sourceContent, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        artifact.setTitle(title);
        artifact.setSourceContent(sourceContent);
        artifact.setRenderedHtml(markdownRenderer.render(sourceContent));
        return artifactRepository.save(artifact);
    }

    @Transactional
    public Artifact updateArtifactStatusOrThrow(Long id, Artifact.Status status, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        artifact.setStatus(status);
        return artifactRepository.save(artifact);
    }

    @Transactional
    public void deleteArtifactOrThrow(Long id, String username) {
        getArtifactOrThrow(id, username);
        artifactRepository.deleteById(id);
    }

    private String generateSlug(String title) {
        String baseSlug = slugifyTitle(title);
        String candidateSlug = baseSlug;
        int suffix = 2;
        while (artifactRepository.findBySlug(candidateSlug).isPresent()) {
            candidateSlug = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidateSlug;
    }

    private String slugifyTitle(String title) {
        String slug = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .replaceAll("-+", "-");

        if (slug.isBlank()) {
            return "artifact";
        }
        return slug;
    }

    private void validateMarkdownFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMarkdownImportException("Markdown file is required");
        }

        if (file.getSize() > MAX_MARKDOWN_FILE_SIZE) {
            throw new InvalidMarkdownImportException("Markdown file must be 2MB or smaller");
        }

        String filename = StringUtils.cleanPath(Optional.ofNullable(file.getOriginalFilename()).orElse(""));
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".md")) {
            throw new InvalidMarkdownImportException("Only .md files are supported");
        }
    }

    private String readMarkdownContent(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new InvalidMarkdownImportException("Markdown file could not be read");
        }
    }

    private Optional<String> normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(title.trim());
    }

    private String titleFromFilename(String originalFilename) {
        String filename = StringUtils.getFilename(Optional.ofNullable(originalFilename).orElse(""));
        if (filename == null || filename.isBlank()) {
            return "Untitled";
        }

        String title = filename.replaceFirst("(?i)\\.md$", "").trim();
        if (title.isBlank()) {
            return "Untitled";
        }
        return title;
    }

    private void validateTitle(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidMarkdownImportException("Title must be 200 characters or fewer");
        }
    }

    private int normalizePage(int page) {
        if (page < 0) {
            throw new InvalidArtifactQueryException("Page must be 0 or greater");
        }
        return page;
    }

    private int normalizePageSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidArtifactQueryException("Size must be between 1 and 100");
        }
        return size;
    }

    private Optional<String> normalizeSearchKeyword(String q) {
        if (q == null || q.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(q.trim());
    }

    private Optional<Artifact.Status> parseStatus(String status) {
        Optional<Artifact.Status> result = Artifact.Status.fromString(status);
        if (result.isEmpty() && status != null && !status.isBlank()) {
            throw new InvalidArtifactQueryException("Status must be draft or published");
        }
        return result;
    }
}
