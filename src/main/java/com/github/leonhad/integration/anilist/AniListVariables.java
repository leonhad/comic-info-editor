package com.github.leonhad.integration.anilist;

import lombok.Getter;

@Getter
public class AniListVariables {

    private final String search;

    public AniListVariables(String search) {
        this.search = search;
    }

}
