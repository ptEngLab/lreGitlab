package com.lre.common.exceptions;

public class ReportFetchException extends RuntimeException {
        public ReportFetchException(String message, Throwable cause) {
            super(message, cause);
        }
        public ReportFetchException(String message) {
            super(message);
        }
    }