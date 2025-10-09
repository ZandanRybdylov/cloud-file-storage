package com.zandan.app.filestorage.exception;

public class QueryIsNotValidException extends RuntimeException {

    public QueryIsNotValidException() {
    }

    public QueryIsNotValidException(String message) {
        super(message);
    }

    public QueryIsNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryIsNotValidException(Throwable cause) {
        super(cause);
    }

    public QueryIsNotValidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
