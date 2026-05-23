package com.chatpress.v1.artifact;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("Artifact slug already exists: " + slug);
    }
}
