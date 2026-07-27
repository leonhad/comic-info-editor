package com.github.leonhad.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageCacheTest {

    @Test
    void testGet() {
        var url = this.getClass().getResource("/images/icon.png");
        assertNotNull(url);

        var image = ImageCache.get(url);
        assertNotNull(image);
    }

    @Test
    void testClear() {
        assertDoesNotThrow(ImageCache::clear);
    }
}