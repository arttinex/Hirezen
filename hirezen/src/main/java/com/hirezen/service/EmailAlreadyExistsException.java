package com.hirezen.service;

/**
 * Thrown when someone tries to sign up with an email already on file.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists.");
    }
}
