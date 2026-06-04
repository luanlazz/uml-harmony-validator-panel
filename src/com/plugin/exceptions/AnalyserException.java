package com.plugin.exceptions;

public class AnalyserException extends Exception {

    public AnalyserException(String message) {
        super(message);
    }

    public AnalyserException(String message, Throwable cause) {
        super(message, cause);
    }
}