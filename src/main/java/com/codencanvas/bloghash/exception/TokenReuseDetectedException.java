package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class TokenReuseDetectedException extends BlogHashException {
    public TokenReuseDetectedException() {
        super("Security alert: token reuse detected. All sessions terminated for your safety", 401);
    }
}