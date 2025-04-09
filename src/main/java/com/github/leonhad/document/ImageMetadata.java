package com.github.leonhad.document;

public class ImageMetadata {

    private final String fileName;

    private final long size;

    public ImageMetadata(String fileName, long size) {
        this.fileName = fileName;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }
}
