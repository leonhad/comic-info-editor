package com.github.leonhad.utils;

public class OSUtils {

    private OSUtils() {
        // Not used.
    }

    public static boolean isOSX() {
        var osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("mac");
    }
}
