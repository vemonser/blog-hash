package com.codencanvas.bloghash.exception.base;

import lombok.Getter;

@Getter
public class BlogHashException extends RuntimeException {
 
    private final int statusCode;
 
    public BlogHashException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
