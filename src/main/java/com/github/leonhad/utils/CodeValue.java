package com.github.leonhad.utils;

public class CodeValue {

    private final String code;

    private final String value;

    public CodeValue(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
