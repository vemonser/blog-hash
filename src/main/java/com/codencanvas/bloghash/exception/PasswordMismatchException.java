package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class PasswordMismatchException extends BlogHashException {
    public PasswordMismatchException() {
        super("Current password is incorrect", 400);
    }
}
