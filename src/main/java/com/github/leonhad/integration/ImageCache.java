package com.github.leonhad.integration;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    public static ImageIcon get(URL url) {
        return CACHE.computeIfAbsent(url.toString(), x -> new ImageIcon(url));
    }

    public static void clear() {
        CACHE.clear();
    }
}
