package com.github.leonhad.document;

public class PageMetadata {

    private final int pageNumber;

    private final int width;

    private final int height;

    private final long size;

    private PageType type = PageType.STORY;

    public PageMetadata(int pageNumber, long size, int width, int height) {
        this.pageNumber = pageNumber;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public PageMetadata(ImageMetadata image, int pageNumber, int width, int height, PageType type) {
        this(pageNumber, image.getSize(), width, height);
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public PageType getType() {
        return type;
    }

    public void setType(PageType type) {
        this.type = type;
    }
}
