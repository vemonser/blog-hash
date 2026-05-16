package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class UsernameAlreadyTakenException extends BlogHashException {
    public UsernameAlreadyTakenException() {
        super("This username is already taken", 409);
    }
}