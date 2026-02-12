package com.vastriantafyllou.bankapp.core.exception;

public class NegativeAmountException extends RuntimeException {

    public NegativeAmountException(String message) {
        super(message);
    }
}
