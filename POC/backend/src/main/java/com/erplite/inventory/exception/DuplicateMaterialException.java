package com.erplite.inventory.exception;

public class DuplicateMaterialException extends RuntimeException {
    public DuplicateMaterialException(String message) {
        super(message);
    }
}
