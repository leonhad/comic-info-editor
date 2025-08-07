package com.github.leonhad.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchExceptionTest {

    @Test
    void testSearchExceptionString() {
        var exception = new SearchException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    void testSearchExceptionStringThrowable() {
        var exception = new SearchException(new Exception("test"));
        assertEquals("java.lang.Exception: test", exception.getMessage());
    }
}