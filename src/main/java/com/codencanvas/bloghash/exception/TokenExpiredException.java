package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class TokenExpiredException extends BlogHashException {
    public TokenExpiredException() {
        super("Token has expired. Please login again", 401);
    }
}
 