package com.med.frida_calculs_app.exception;

public class HeritageCalculationException extends RuntimeException {

    public HeritageCalculationException(String message) {
        super(message);
    }

    public HeritageCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
