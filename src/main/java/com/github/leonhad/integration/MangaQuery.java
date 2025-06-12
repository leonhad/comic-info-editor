package com.github.leonhad.integration;

import com.github.leonhad.exception.SearchException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class MangaQuery {

    public abstract List<Manga> search(String query) throws SearchException;

    protected Double getDouble(Map<String, Object> map, String key) {
        return Optional.ofNullable((Number) map.get(key)).map(Number::doubleValue).orElse(null);
    }

    protected Integer getInteger(Map<String, Object> map, String key) {
        return Optional.ofNullable((Number) map.get(key)).map(Number::intValue).orElse(null);
    }

    protected String getString(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key)).map(Object::toString).orElse(null);
    }

    protected URL getURL(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .map(Object::toString)
                .map(x -> {
                    try {
                        return new URL(x);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).orElse(null);
    }
}
