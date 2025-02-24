package com.github.leonhad.fileformat;

import com.github.leonhad.document.ImageMetadata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class FileOpener {

    private int index;

    public abstract int getImageCount();

    protected abstract void loadImage() throws IOException;

    public abstract BufferedImage readImage(ImageMetadata image) throws IOException;

    protected abstract FileFilter[] getFilters();

    public abstract void open(File file) throws IOException;

    public abstract boolean getComicInfo(Function<InputStream, Boolean> consumer) throws IOException;

    public abstract List<ImageMetadata> getImageList();

    public abstract BufferedImage getCurrentImage();

    public boolean canOpen(File file) {
        var filters = getFilters();
        return Arrays.stream(filters).anyMatch(f -> f.accept(file));
    }

    protected BufferedImage loadImage(InputStream inputStream) throws IOException {
        try (var buffer = new BufferedInputStream(inputStream)) {
            return ImageIO.read(buffer);
        }
    }

    public BufferedImage nextPage() throws IOException {
        index++;
        if (index >= getImageCount()) {
            index = getImageCount() - 1;
        }

        loadImage();
        return getCurrentImage();
    }

    public BufferedImage previousPage() throws IOException {
        index--;
        if (index < 0) {
            index = 0;
        }

        loadImage();
        return getCurrentImage();
    }

    public BufferedImage firstPage() throws IOException {
        index = 0;
        loadImage();
        return getCurrentImage();
    }

    public BufferedImage lastPage() throws IOException {
        index = getImageCount() - 1;
        loadImage();
        return getCurrentImage();
    }

    public int getIndex() {
        return index;
    }
}
