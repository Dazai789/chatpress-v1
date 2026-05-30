package com.chatpress.artifact;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatpress.artifact.exception.ArtifactNotFoundException;
import com.chatpress.artifact.exception.InvalidArtifactQueryException;
import com.chatpress.artifact.exception.InvalidMarkdownImportException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ArtifactService {

    private static final long MAX_MARKDOWN_FILE_SIZE = 2 * 1024 * 1024;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    private final ArtifactMapper artifactMapper;
    private final TagRepository tagRepository;
    private final MarkdownRenderer markdownRenderer;
    private final PublicPageCache publicPageCache;
    private final AsyncTaskService asyncTaskService;

    public ArtifactService(ArtifactMapper artifactMapper,
                           TagRepository tagRepository,
                           MarkdownRenderer markdownRenderer,
                           PublicPageCache publicPageCache,
                           AsyncTaskService asyncTaskService) {
        this.artifactMapper = artifactMapper;
        this.tagRepository = tagRepository;
        this.markdownRenderer = markdownRenderer;
        this.publicPageCache = publicPageCache;
        this.asyncTaskService = asyncTaskService;
    }

    @Transactional
    public Artifact createArtifact(String title, String sourceContent, List<String> tagNames, String username) {
        String finalSlug = generateSlug(title);

        Artifact artifact = new Artifact(title, finalSlug, sourceContent, markdownRenderer.render(sourceContent), username);
        artifact.setStatus(Artifact.Status.PUBLISHED);
        artifact.setTags(resolveTags(tagNames));
        artifactMapper.insert(artifact);
        asyncTaskService.afterPublish(artifact.getSlug(), artifact.getTitle(), artifact.getSourceContent());
        return artifact;
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

        return createArtifact(finalTitle, sourceContent, null, username);
    }

    public IPage<Artifact> listArtifacts(int page, int size, String q, String status, String tag, String username) {
        var pageReq = new Page<Artifact>(normalizePage(page) + 1, normalizePageSize(size)); // MyBatis-Plus pages start at 1

        Optional<String> keyword = normalizeSearchKeyword(q);
        Optional<String> tagFilter = normalizeSearchKeyword(tag);
        String statusValue = parseStatus(status).map(s -> s.name()).orElse(null);

        return artifactMapper.searchArtifacts(
                pageReq,
                username,
                keyword.orElse(null),
                tagFilter.orElse(null),
                statusValue
        );
    }

    public Artifact getArtifactOrThrow(Long id, String username) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new ArtifactNotFoundException(id);
        }
        if (!artifact.getCreatedBy().equals(username)) {
            throw new ArtifactNotFoundException(id);
        }
        return artifact;
    }

    public Optional<Artifact> getArtifactBySlug(String slug) {
        return artifactMapper.findBySlug(slug);
    }

    public Optional<Artifact> getPublishedArtifactBySlug(String slug) {
        return artifactMapper.findBySlugAndStatus(slug, Artifact.Status.PUBLISHED.name());
    }

    @Transactional
    public Artifact updateArtifactOrThrow(Long id, String title, String sourceContent, List<String> tagNames, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        artifact.setTitle(title);
        artifact.setSourceContent(sourceContent);
        artifact.setRenderedHtml(markdownRenderer.render(sourceContent));
        artifact.setTags(resolveTags(tagNames));
        artifactMapper.updateById(artifact);
        publicPageCache.evict(artifact.getSlug());
        return artifact;
    }

    @Transactional
    public Artifact updateArtifactStatusOrThrow(Long id, Artifact.Status status, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        artifact.setStatus(status);
        artifactMapper.updateById(artifact);
        publicPageCache.evict(artifact.getSlug());
        return artifact;
    }

    @Transactional
    public Artifact updateArtifactWithStatusOrThrow(Long id, String title, String sourceContent, List<String> tagNames, Artifact.Status status, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        artifact.setTitle(title);
        artifact.setSourceContent(sourceContent);
        artifact.setRenderedHtml(markdownRenderer.render(sourceContent));
        artifact.setStatus(status);
        artifact.setTags(resolveTags(tagNames));
        artifactMapper.updateById(artifact);
        publicPageCache.evict(artifact.getSlug());
        return artifact;
    }

    @Transactional
    public void deleteArtifactOrThrow(Long id, String username) {
        Artifact artifact = getArtifactOrThrow(id, username);
        publicPageCache.evict(artifact.getSlug());
        artifactMapper.deleteById(id);
    }

    private String generateSlug(String title) {
        String baseSlug = slugifyTitle(title);

        var existingSlugs = artifactMapper.findSlugsByPrefix(baseSlug);

        if (!existingSlugs.contains(baseSlug)) {
            return baseSlug;
        }

        // Find the highest existing suffix and add 1
        int maxSuffix = 1;
        String prefix = baseSlug + "-";
        for (String slug : existingSlugs) {
            if (slug.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(slug.substring(prefix.length()));
                    if (num > maxSuffix) {
                        maxSuffix = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return baseSlug + "-" + (maxSuffix + 1);
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

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String normalized = name.trim().toLowerCase(Locale.ROOT);
            if (normalized.length() > 50) {
                continue;
            }
            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> tagRepository.save(new Tag(normalized)));
            tags.add(tag);
        }
        return tags;
    }
}
