package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class ResourceNotFoundException extends BlogHashException {
    public ResourceNotFoundException(String resource) {
        super(resource + " not found", 404);
    }
}
 