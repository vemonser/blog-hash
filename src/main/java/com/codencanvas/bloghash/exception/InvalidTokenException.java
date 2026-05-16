package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class InvalidTokenException extends BlogHashException {
    public InvalidTokenException(String message) {
        super(message, 401);
    }
}