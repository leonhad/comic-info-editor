package com.github.leonhad.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PageMetadata {

    private final ZipEntry entry;

    private final int pageNumber;

    private final int width;

    private final int height;

    private PageType type = PageType.STORY;

    public PageMetadata(ZipEntry entry, int pageNumber, int width, int height) {
        this.entry = entry;
        this.pageNumber = pageNumber;
        this.width = width;
        this.height = height;
    }

    public InputStream createInputStream(ZipFile zipFile) throws IOException {
        return zipFile.getInputStream(entry);
    }

    public long getSize() {
        return entry.getSize();
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
