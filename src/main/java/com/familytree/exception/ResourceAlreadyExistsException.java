package com.familytree.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
    
    public ResourceAlreadyExistsException(String resource, String identifier) {
        super(String.format("%s already exists with identifier: %s", resource, identifier));
    }
}
