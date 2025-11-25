package com.BFB.automobile.business.exception;

/**
 * Exception métier pour les violations de règles métier
 * 
 * DESIGN PATTERN GoF UTILISÉ :
 * 
 * TEMPLATE METHOD PATTERN (implicite) : Cette classe hérite de RuntimeException
 * et réutilise le template défini par la hiérarchie d'exceptions Java.
 * Les constructeurs multiples permettent différentes façons de créer l'exception
 * tout en conservant la même structure de base.
 * 
 * Avantages :
 * - Code d'erreur structuré pour un traitement programmatique
 * - Message lisible pour les utilisateurs
 * - Gestion centralisée via GlobalExceptionHandler
 * - RuntimeException = pas besoin de try-catch partout (unchecked exception)
 */
public class BusinessException extends RuntimeException {
    
    private final String code;
    
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BUSINESS_ERROR";
    }
    
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
