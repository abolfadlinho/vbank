package com.vbank.bffservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DownstreamServiceException extends RuntimeException {
    private final HttpStatus status;

    public DownstreamServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}