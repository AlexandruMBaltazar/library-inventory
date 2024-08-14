package com.learnkafka.exception;

public class RetryableMessagingException extends RuntimeException {

    public RetryableMessagingException(String message) {
        super(message);
    }

    public RetryableMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
