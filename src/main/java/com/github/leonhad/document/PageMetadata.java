package com.github.leonhad.document;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
public class PageMetadata {

    private final int pageNumber;

    private final int width;

    private final int height;

    private final long size;

    @Setter
    private PageType type = PageType.STORY;

    public PageMetadata(int pageNumber, long size, int width, int height) {
        this.pageNumber = pageNumber;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public PageMetadata(ImageMetadata image, int pageNumber, int width, int height, PageType type) {
        this(pageNumber, Optional.ofNullable(image.getImage()).map(b -> (long) b.length).orElse(0L), width, height);
        this.type = type;
    }

}
