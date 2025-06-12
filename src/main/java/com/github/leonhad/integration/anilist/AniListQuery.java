package com.github.leonhad.integration.anilist;

public class AniListQuery {

    private final String query;

    private final AniListVariables variables;

    public AniListQuery(String query, String search) {
        this.query = query.replace("\n", "").replace("\r", "").replace("\"", "\\\"");
        this.variables = new AniListVariables(search);
    }

    public String getQuery() {
        return query;
    }

    public AniListVariables getVariables() {
        return variables;
    }
}
