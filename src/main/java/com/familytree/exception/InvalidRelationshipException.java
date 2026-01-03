package com.familytree.exception;

/**
 * Exception thrown when an invalid relationship is attempted.
 */
public class InvalidRelationshipException extends RuntimeException {
    
    public InvalidRelationshipException(String message) {
        super(message);
    }
}
