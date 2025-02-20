package com.github.leonhad.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ISOLanguage {

    private final String isoCode;

    private final String language;

    public ISOLanguage(String isoCode, String language) {
        this.isoCode = isoCode;

        if (language == null || language.isEmpty()) {
            this.language = "Undefined";
        } else {
            this.language = Character.toUpperCase(language.charAt(0)) + language.substring(1);
        }
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return language + " (" + isoCode + ")";
    }

    public static List<ISOLanguage> list() {
        return Arrays.stream(Locale.getAvailableLocales())
                .filter(locale -> !locale.getLanguage().equals("und"))
                .map(x -> new ISOLanguage(x.toLanguageTag(), x.getDisplayLanguage()))
                .sorted(Comparator.comparing(ISOLanguage::getLanguage))
                .collect(Collectors.toList());
    }
}
