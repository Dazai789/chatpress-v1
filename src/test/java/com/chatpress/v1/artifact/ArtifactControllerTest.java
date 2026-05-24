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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Java Notes</h1>\n"));
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

        Integer artifactId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/artifacts/{id}", artifactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artifactId))
                .andExpect(jsonPath("$.title").value("Backend Notes"))
                .andExpect(jsonPath("$.slug").value("backend-notes"))
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.renderedHtml").value("<h1>Backend Notes</h1>\n"));
    }

    @Test
    void getPublicPageBySlug() throws Exception {
        createArtifact("Public Notes", "public-notes", "# Public Notes")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/p/public-notes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string("<h1>Public Notes</h1>\n"));
    }

    private ResultActions createArtifact(
            String title,
            String slug,
            String sourceContent
    ) throws Exception {
        return mockMvc.perform(post("/api/artifacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "slug", slug,
                        "sourceContent", sourceContent
                ))));
    }
}
