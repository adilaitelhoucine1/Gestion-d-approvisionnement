package com.tricol.gestionstock.exception;

import com.tricol.gestionstock.entity.CommandeFournisseur;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Une erreur interne est survenue."));
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public  ResponseEntity<?> handleDuplicateResource(DuplicateResourceException duplicateResourceException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Erreur",duplicateResourceException.getMessage()));
    }
    @ExceptionHandler(CommandeNotReceptionnableException.class)

    public ResponseEntity<?> handleCommandeNotReceptionnable(CommandeNotReceptionnableException cmd){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("Erreur" , cmd.getMessage()));
    }
    @ExceptionHandler(IllegalStateException.class)
    public  ResponseEntity<?> handleIllegalStateException(IllegalStateException mess){
        return   ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("Erruer ",mess.getMessage()));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public  ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException mess){
        return   ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Erruer ",mess.getMessage()));
    }
}
