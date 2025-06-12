package com.github.leonhad.integration.anilist;

import com.github.leonhad.exception.SearchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AniListTest {

    @Test
    void testSearch() throws SearchException {
        var anilist = new AniList();

        var ret = anilist.search("COWA!");
        assertFalse(ret.isEmpty());
        assertEquals("COWA!", ret.get(0).getTitle());
    }
}