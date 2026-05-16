package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class AccountNotVerifiedException extends BlogHashException {
    public AccountNotVerifiedException() {
        super("Please verify your email address before logging in", 403);
    }
}