package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;


public class InvalidCredentialsException extends BlogHashException {
    public InvalidCredentialsException() {
        super("Invalid credentials", 401);
    }
}
