package com.chatpress.artifact;

import com.chatpress.common.OperationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class ArtifactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private PublicPageCache publicPageCache;

    @Test
    void createArtifact() throws Exception {
        createArtifact("Java Notes", "# Java Notes")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Notes"))
                .andExpect(jsonPath("$.slug").value("java-notes"))
                .andExpect(jsonPath("$.sourceFormat").value("markdown"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Java Notes</h1>\n"));
    }

    @Test
    void createArtifactGeneratesSlugWhenMissing() throws Exception {
        createArtifact("Spring Boot Notes", "# Spring Boot Notes")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring Boot Notes"))
                .andExpect(jsonPath("$.slug").value("spring-boot-notes"));
    }

    @Test
    void createArtifactGeneratesUniqueSlugWhenTitleRepeats() throws Exception {
        createArtifact("Repeat Notes", "# Repeat Notes")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("repeat-notes"));

        createArtifact("Repeat Notes", "# Repeat Notes Again")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("repeat-notes-2"));
    }

    @Test
    void createArtifactUsesFallbackSlugWhenTitleHasNoUrlFriendlyText() throws Exception {
        createArtifact("中文标题", "# 中文标题")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("artifact"));
    }

    @Test
    void importMarkdownFileWithTitle() throws Exception {
        MockMultipartFile file = markdownFile("macdown.md", "# Imported Notes");

        mockMvc.perform(multipart("/api/artifacts/import/markdown")
                        .file(file)
                        .param("title", "Imported From MacDown")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Imported From MacDown"))
                .andExpect(jsonPath("$.slug").value("imported-from-macdown"))
                .andExpect(jsonPath("$.sourceFormat").value("markdown"))
                .andExpect(jsonPath("$.sourceContent").value("# Imported Notes"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Imported Notes</h1>\n"));
    }

    @Test
    void importMarkdownFileUsesFilenameWhenTitleMissing() throws Exception {
        MockMultipartFile file = markdownFile("MacDown Export.md", "# Filename Notes");

        mockMvc.perform(multipart("/api/artifacts/import/markdown")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("MacDown Export"))
                .andExpect(jsonPath("$.slug").value("macdown-export"));
    }

    @Test
    void rejectNonMarkdownImportFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "# Notes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/artifacts/import/markdown")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MARKDOWN_FILE"))
                .andExpect(jsonPath("$.message").value("Only .md files are supported"));
    }

    @Test
    void rejectEmptyMarkdownImportFile() throws Exception {
        MockMultipartFile file = markdownFile("empty.md", "");

        mockMvc.perform(multipart("/api/artifacts/import/markdown")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MARKDOWN_FILE"))
                .andExpect(jsonPath("$.message").value("Markdown file is required"));
    }

    @Test
    void rejectOversizedMarkdownImportFile() throws Exception {
        byte[] content = new byte[(2 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.md",
                "text/markdown",
                content
        );

        mockMvc.perform(multipart("/api/artifacts/import/markdown")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MARKDOWN_FILE"))
                .andExpect(jsonPath("$.message").value("Markdown file must be 2MB or smaller"));
    }

    @Test
    void rejectInvalidRequest() throws Exception {
        createArtifact("", "# Invalid Request")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void returnNotFoundForMissingArtifact() throws Exception {
        mockMvc.perform(get("/api/artifacts/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Artifact not found: 999999"))
                .andExpect(jsonPath("$.fields").doesNotExist());
    }

    @Test
    void rejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void rejectInvalidPathVariableType() throws Exception {
        mockMvc.perform(get("/api/artifacts/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PATH_VARIABLE"))
                .andExpect(jsonPath("$.message").value("Path variable has invalid type"));
    }

    @Test
    void rejectUnsupportedMethod() throws Exception {
        mockMvc.perform(patch("/api/artifacts/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("HTTP method is not supported"));
    }

    @Test
    void rejectUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("title=Plain Text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message").value("Content type is not supported"));
    }

    @Test
    void getArtifactById() throws Exception {
        MvcResult result = createArtifact("Backend Notes", "# Backend Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(get("/api/artifacts/{id}", artifactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.title").value("Backend Notes"))
                .andExpect(jsonPath("$.slug").value("backend-notes"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Backend Notes</h1>\n"));
    }

    @Test
    void listArtifactsReturnsSummary() throws Exception {
        createArtifact("Older List Notes", "# Older List Notes")
                .andExpect(status().isCreated());
        createArtifact("Newer List Notes", "# Newer List Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/artifacts")
                        .param("q", "List Notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Newer List Notes"))
                .andExpect(jsonPath("$.items[0].slug").value("newer-list-notes"))
                .andExpect(jsonPath("$.items[0].sourceFormat").value("markdown"))
                .andExpect(jsonPath("$.items[0].status").value("published"))
                .andExpect(jsonPath("$.items[0].sourceContent").doesNotExist())
                .andExpect(jsonPath("$.items[0].renderedHtml").doesNotExist())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listArtifactsSupportsPagination() throws Exception {
        createArtifact("First Page Notes", "# First Page Notes")
                .andExpect(status().isCreated());
        createArtifact("Second Page Notes", "# Second Page Notes")
                .andExpect(status().isCreated());
        createArtifact("Third Page Notes", "# Third Page Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/artifacts")
                        .param("q", "Page Notes")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Second Page Notes"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listArtifactsSupportsStatusFilter() throws Exception {
        createArtifact("Published Filter Notes", "# Published Filter Notes")
                .andExpect(status().isCreated());
        MvcResult draftResult = createArtifact("Draft Filter Notes", "# Draft Filter Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long draftArtifactId = artifactIdFrom(draftResult);

        mockMvc.perform(put("/api/artifacts/{id}/status", draftArtifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/artifacts")
                        .param("q", "Filter Notes")
                        .param("status", "draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Draft Filter Notes"))
                .andExpect(jsonPath("$.items[0].status").value("draft"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void rejectInvalidListQueryParameters() throws Exception {
        mockMvc.perform(get("/api/artifacts")
                        .param("status", "archived"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_QUERY_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Status must be draft or published"));
    }

    @Test
    void updateArtifact() throws Exception {
        MvcResult result = createArtifact("Old Notes", "# Old Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}", artifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Updated Notes", "# Updated Notes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.title").value("Updated Notes"))
                .andExpect(jsonPath("$.slug").value("old-notes"))
                .andExpect(jsonPath("$.sourceFormat").value("markdown"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Updated Notes</h1>\n"));
    }

    @Test
    void updateArtifactStatus() throws Exception {
        MvcResult result = createArtifact("Status Notes", "# Status Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}/status", artifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.status").value("draft"));
    }

    @Test
    void deleteArtifact() throws Exception {
        MvcResult result = createArtifact("Delete Notes", "# Delete Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(delete("/api/artifacts/{id}", artifactId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/artifacts/{id}", artifactId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Artifact not found: " + artifactId));
    }

    @Test
    void getPublicPageBySlug() throws Exception {
        createArtifact("Public <Notes> & Tips", "# Public Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/p/public-notes-tips"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!doctype html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<title>Public &lt;Notes&gt; &amp; Tips</title>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("max-width: 780px;")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<h1>Public Notes</h1>")));
    }

    @Test
    void publicPageSupportsCommonMarkdownContent() throws Exception {
        String markdown = """
                # Markdown Guide

                ## Backend Notes

                - Controller
                - Service

                > Keep the public page readable.

                Use `MockMvc` for tests.

                ```
                System.out.println("hello");
                ```

                Read [Spring](https://spring.io).
                """;

        createArtifact("Markdown Guide", markdown)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/p/markdown-guide"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<h2>Backend Notes</h2>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<li>Controller</li>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<blockquote>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<code>MockMvc</code>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<pre><code>System.out.println(\"hello\");")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<a href=\"https://spring.io\">Spring</a>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("blockquote {")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("pre code {")));
    }

    @Test
    void returnNotFoundForDraftPublicPage() throws Exception {
        MvcResult result = createArtifact("Draft Notes", "# Draft Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}/status", artifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/p/draft-notes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessOtherUsersArtifact() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .with(user("alice").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Alice Notes", "# Alice Notes")))
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(get("/api/artifacts/{id}", artifactId)
                        .with(user("bob").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"));
    }

    @Test
    void userCannotEditOtherUsersArtifact() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .with(user("alice").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Alice Edit Notes", "# Alice Edit Notes")))
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}", artifactId)
                        .with(csrf())
                        .with(user("bob").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Bob Tries Edit", "# Bob Tries Edit")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"));
    }

    @Test
    void userCannotDeleteOtherUsersArtifact() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .with(user("alice").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Alice Delete Notes", "# Alice Delete Notes")))
                .andExpect(status().isCreated())
                .andReturn();

        Long artifactId = artifactIdFrom(result);

        mockMvc.perform(delete("/api/artifacts/{id}", artifactId)
                        .with(csrf())
                        .with(user("bob").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"));
    }

    @Test
    void userCannotListOtherUsersArtifacts() throws Exception {
        mockMvc.perform(post("/api/artifacts")
                        .with(csrf())
                        .with(user("alice").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Alice Private Notes", "# Alice Private Notes")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/artifacts")
                        .with(user("bob").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void publicPageCacheHit() throws Exception {
        createArtifact("Cache Test", "# Cache Test")
                .andExpect(status().isCreated());

        // First request: cache miss, should populate cache
        mockMvc.perform(get("/p/cache-test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<h1>Cache Test</h1>")));

        // Verify cache has the entry
        String cached = publicPageCache.get("cache-test");
        assertThat(cached).isNotNull();
        assertThat(cached).contains("<h1>Cache Test</h1>");
    }

    @Test
    void publicPageCacheInvalidatedOnUpdate() throws Exception {
        MvcResult result = createArtifact("Cache Update", "# Before Update")
                .andExpect(status().isCreated())
                .andReturn();
        Long artifactId = artifactIdFrom(result);

        // Populate cache
        mockMvc.perform(get("/p/cache-update"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Before Update")));

        // Update artifact (via JSON API)
        mockMvc.perform(put("/api/artifacts/{id}", artifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Cache Update", "# After Update")))
                .andExpect(status().isOk());

        // Cache should be evicted, new request gets fresh content
        mockMvc.perform(get("/p/cache-update"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("After Update")));
    }

    @Test
    void publicPageCacheInvalidatedOnDelete() throws Exception {
        MvcResult result = createArtifact("Cache Delete", "# Delete Me")
                .andExpect(status().isCreated())
                .andReturn();
        Long artifactId = artifactIdFrom(result);

        // Populate cache
        mockMvc.perform(get("/p/cache-delete"))
                .andExpect(status().isOk());

        // Delete artifact
        mockMvc.perform(delete("/api/artifacts/{id}", artifactId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Cache evicted, page returns 404
        mockMvc.perform(get("/p/cache-delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void draftContentNotCachedAsPublic() throws Exception {
        MvcResult result = createArtifact("Draft Cache Test", "# Draft Cache")
                .andExpect(status().isCreated())
                .andReturn();
        Long artifactId = artifactIdFrom(result);

        // Set to draft
        mockMvc.perform(put("/api/artifacts/{id}/status", artifactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk());

        // Draft should return 404
        mockMvc.perform(get("/p/draft-cache-test"))
                .andExpect(status().isNotFound());

        // Cache should NOT contain draft content
        String cached = publicPageCache.get("draft-cache-test");
        assertThat(cached).isNull();
    }

    @Test
    void publicListPageShowsPublishedArticles() throws Exception {
        createArtifact("Public List One", "# One").andExpect(status().isCreated());

        // Create an article and set it to draft
        MvcResult draftResult = createArtifact("Public Draft Two", "# Draft")
                .andExpect(status().isCreated())
                .andReturn();
        Long draftId = artifactIdFrom(draftResult);
        mockMvc.perform(put("/api/artifacts/{id}/status", draftId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk());

        // Public list should only show published
        mockMvc.perform(get("/p"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<title>ChatPress</title>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Public List One")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Public Draft Two")
                )));
    }

    private ResultActions createArtifact(
            String title,
            String sourceContent
    ) throws Exception {
        return mockMvc.perform(post("/api/artifacts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(artifactJson(title, sourceContent)));
    }

    private String artifactJson(String title, String sourceContent) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "sourceContent", sourceContent
        ));
    }

    private String statusJson(String status) throws Exception {
        return objectMapper.writeValueAsString(Map.of("status", status));
    }

    private MockMultipartFile markdownFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/markdown",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Long artifactIdFrom(MvcResult result) throws Exception {
        Object id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id instanceof Integer ? ((Integer) id).longValue() : (Long) id;
    }
}
