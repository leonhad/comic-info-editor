package com.github.leonhad.document;

import javax.swing.*;

public enum PageType {

    FRONT_COVER("FrontCover", "Front cover", KeyStroke.getKeyStroke("F2")),
    INNER_COVER("InnerCover", "Inner cover", KeyStroke.getKeyStroke("F3")),
    ROUNDUP("Roundup", "Roundup", null),
    STORY("Story", "Story", KeyStroke.getKeyStroke("F4")),
    ADVERTISEMENT("Advertisement", "Advertisement", KeyStroke.getKeyStroke("F5")),
    EDITORIAL("Editorial", "Editorial", null),
    LETTERS("Letters", "Letters", KeyStroke.getKeyStroke("F6")),
    PAGE_TYPE("Preview", "Preview", null),
    BACK_COVER("BackCover", "Back cover", KeyStroke.getKeyStroke("F7")),
    OTHER("Other", "Other", KeyStroke.getKeyStroke("F8")),
    DELETED("Deleted", "Deleted", null);

    private final String text;

    private final String description;

    private final KeyStroke keyStroke;

    PageType(String text, String description, KeyStroke keyStroke) {
        this.text = text;
        this.description = description;
        this.keyStroke = keyStroke;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }
}
