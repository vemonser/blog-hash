package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class EmailAlreadyExistsException extends BlogHashException {
    public EmailAlreadyExistsException() {
        super("An account with this email already exists", 409);
    }
}