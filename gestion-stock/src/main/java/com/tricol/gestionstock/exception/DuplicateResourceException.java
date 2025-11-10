package com.tricol.gestionstock.exception;

public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String fieldName, Object fieldValue) {
        super(String.format("Un enrgestrement %s '%s' deja exist",fieldName, fieldValue));
    }
}
