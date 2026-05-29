package com.chatpress.artifact.exception;

public class ArtifactNotFoundException extends RuntimeException {

    public ArtifactNotFoundException(Long id) {
        super("Artifact not found: " + id);
    }
}
