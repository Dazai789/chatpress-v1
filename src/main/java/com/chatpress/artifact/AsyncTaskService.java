package com.chatpress.artifact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskService {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskService.class);

    @Async
    public void afterPublish(String slug, String title, String sourceContent) {
        int wordCount = sourceContent.split("\\s+").length;
        int charCount = sourceContent.length();
        log.info("Artifact published: slug={}, title={}, words={}, chars={}", slug, title, wordCount, charCount);
    }
}
