package com.vastriantafyllou.bankapp.core.exception;

public class AccountNumberAlreadyExistsException extends RuntimeException {

    public AccountNumberAlreadyExistsException(String message) {
        super(message);
    }
}
