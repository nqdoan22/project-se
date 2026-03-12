package com.erplite.inventory.exception;

public class MaterialNotFoundException extends RuntimeException {
    public MaterialNotFoundException(String materialId) {
        super("Material not found: " + materialId);
    }
}
