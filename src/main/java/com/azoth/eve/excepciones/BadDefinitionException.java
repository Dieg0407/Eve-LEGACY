package com.azoth.eve.excepciones;

public class BadDefinitionException extends Exception {

    public BadDefinitionException() { super(); }
    public BadDefinitionException(String message) { super(message); }
    public BadDefinitionException(String message, Throwable cause) { super(message, cause); }
    public BadDefinitionException(Throwable cause) { super(cause); }
}
