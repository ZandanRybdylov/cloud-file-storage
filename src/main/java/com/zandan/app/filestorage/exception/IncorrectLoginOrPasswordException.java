package com.zandan.app.filestorage.exception;

public class IncorrectLoginOrPasswordException extends RuntimeException {

    public IncorrectLoginOrPasswordException() {
    }

    public IncorrectLoginOrPasswordException(String message) {
        super(message);
    }

    public IncorrectLoginOrPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectLoginOrPasswordException(Throwable cause) {
        super(cause);
    }

    public IncorrectLoginOrPasswordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
