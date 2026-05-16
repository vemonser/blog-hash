package com.codencanvas.bloghash.exception;

import com.codencanvas.bloghash.exception.base.BlogHashException;

public class AccountLockedException extends BlogHashException {
    public AccountLockedException(long remainingMinutes) {
        super("Account temporarily locked. Try again in " + remainingMinutes + " minute(s)", 423);
    }
}