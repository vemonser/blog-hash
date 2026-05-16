package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class InvalidVerificationTokenException extends BlogHashException {
    public InvalidVerificationTokenException() {
        super("Invalid or expired verification token", 400);
    }
}