package com.github.saserg.webclient;

import org.springframework.http.HttpStatus;

@FunctionalInterface
public interface OnError {
    void error(HttpStatus status, String error);
}
