package com.chatpress.v1.artifact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtifactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createArtifact() throws Exception {
        createArtifact("Java Notes", "java-notes", "# Java Notes")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Notes"))
                .andExpect(jsonPath("$.slug").value("java-notes"))
                .andExpect(jsonPath("$.sourceType").value("markdown"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Java Notes</h1>\n"));
    }

    @Test
    void createAiChatArtifact() throws Exception {
        createArtifact("AI Chat Notes", "ai-chat-notes", "ai_chat", "User: What is Spring Boot?\nAssistant: A Java framework.")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("AI Chat Notes"))
                .andExpect(jsonPath("$.slug").value("ai-chat-notes"))
                .andExpect(jsonPath("$.sourceType").value("ai_chat"))
                .andExpect(jsonPath("$.status").value("published"));
    }

    @Test
    void rejectDuplicateSlug() throws Exception {
        createArtifact("First Note", "duplicate-slug", "# First Note")
                .andExpect(status().isCreated());

        createArtifact("Second Note", "duplicate-slug", "# Second Note")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_SLUG"))
                .andExpect(jsonPath("$.message").value("Artifact slug already exists: duplicate-slug"));
    }

    @Test
    void rejectInvalidRequest() throws Exception {
        createArtifact("", "invalid-request", "# Invalid Request")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void returnNotFoundForMissingArtifact() throws Exception {
        mockMvc.perform(get("/api/artifacts/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Artifact not found: 999999"));
    }

    @Test
    void getArtifactById() throws Exception {
        MvcResult result = createArtifact("Backend Notes", "backend-notes", "# Backend Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Integer artifactId = artifactIdFrom(result);

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
        createArtifact("Older List Notes", "older-list-notes", "# Older List Notes")
                .andExpect(status().isCreated());
        createArtifact("Newer List Notes", "newer-list-notes", "# Newer List Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/artifacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Newer List Notes"))
                .andExpect(jsonPath("$[0].slug").value("newer-list-notes"))
                .andExpect(jsonPath("$[0].sourceType").value("markdown"))
                .andExpect(jsonPath("$[0].status").value("published"))
                .andExpect(jsonPath("$[0].sourceContent").doesNotExist())
                .andExpect(jsonPath("$[0].renderedHtml").doesNotExist());
    }

    @Test
    void updateArtifact() throws Exception {
        MvcResult result = createArtifact("Old Notes", "old-notes", "# Old Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Integer artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}", artifactId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactJson("Updated Notes", "updated-notes", "# Updated Notes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.title").value("Updated Notes"))
                .andExpect(jsonPath("$.slug").value("updated-notes"))
                .andExpect(jsonPath("$.sourceType").value("markdown"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Updated Notes</h1>\n"));
    }

    @Test
    void updateArtifactStatus() throws Exception {
        MvcResult result = createArtifact("Status Notes", "status-notes", "# Status Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Integer artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}/status", artifactId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.status").value("draft"));
    }

    @Test
    void deleteArtifact() throws Exception {
        MvcResult result = createArtifact("Delete Notes", "delete-notes", "# Delete Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Integer artifactId = artifactIdFrom(result);

        mockMvc.perform(delete("/api/artifacts/{id}", artifactId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/artifacts/{id}", artifactId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTIFACT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Artifact not found: " + artifactId));
    }

    @Test
    void getPublicPageBySlug() throws Exception {
        createArtifact("Public <Notes> & Tips", "public-notes", "# Public Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/p/public-notes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!doctype html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<title>Public &lt;Notes&gt; &amp; Tips</title>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("max-width: 760px;")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<h1>Public Notes</h1>")));
    }

    @Test
    void returnNotFoundForDraftPublicPage() throws Exception {
        MvcResult result = createArtifact("Draft Notes", "draft-notes", "# Draft Notes")
                .andExpect(status().isCreated())
                .andReturn();

        Integer artifactId = artifactIdFrom(result);

        mockMvc.perform(put("/api/artifacts/{id}/status", artifactId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("draft")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/p/draft-notes"))
                .andExpect(status().isNotFound());
    }

    private ResultActions createArtifact(
            String title,
            String slug,
            String sourceContent
    ) throws Exception {
        return mockMvc.perform(post("/api/artifacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(artifactJson(title, slug, sourceContent)));
    }

    private ResultActions createArtifact(
            String title,
            String slug,
            String sourceType,
            String sourceContent
    ) throws Exception {
        return mockMvc.perform(post("/api/artifacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(artifactJson(title, slug, sourceType, sourceContent)));
    }

    private String artifactJson(String title, String slug, String sourceContent) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "slug", slug,
                "sourceContent", sourceContent
        ));
    }

    private String artifactJson(String title, String slug, String sourceType, String sourceContent) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "slug", slug,
                "sourceType", sourceType,
                "sourceContent", sourceContent
        ));
    }

    private String statusJson(String status) throws Exception {
        return objectMapper.writeValueAsString(Map.of("status", status));
    }

    private Integer artifactIdFrom(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }
}
