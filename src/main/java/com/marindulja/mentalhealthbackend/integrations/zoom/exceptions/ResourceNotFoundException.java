package com.marindulja.mentalhealthbackend.integrations.zoom.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException (String message) {
        super(message);
    }
}
