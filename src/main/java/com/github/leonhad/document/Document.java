package com.github.leonhad.document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Document {

    private final File file;
    private int index;
    private BufferedImage currentImage;

    private final List<ZipEntry> imageList = new ArrayList<>();
    private final List<ZipEntry> xmlList = new ArrayList<>();

    public Document(File file) throws IOException {
        this.file = file;

        try (var zipFile = new ZipFile(file)) {
            zipFile.stream().forEach((entry) -> {
                var name = entry.getName();
                if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    imageList.add(entry);
                } else if (name.endsWith(".xml")) {
                    xmlList.add(entry);
                }
            });
        }

        imageList.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));
        xmlList.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));

        imageList.stream().findFirst().orElseThrow(() -> new IOException("No image found"));
        loadImage();
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public int getWidth() {
        return currentImage.getWidth();
    }

    public int getHeight() {
        return currentImage.getHeight();
    }

    private void loadImage() throws IOException {
        try (var zipFile = new ZipFile(file); var stream = zipFile.getInputStream(imageList.get(index))) {
            this.currentImage = ImageIO.read(stream);
        }
    }

    public void nextPage() throws IOException {
        index++;
        if (index >= imageList.size()) {
            index = imageList.size() - 1;
        }

        loadImage();
    }

    public void previousPage() throws IOException {
        index--;
        if (index < 0) {
            index = 0;
        }

        loadImage();
    }

    public void firstPage() throws IOException {
        index = 0;
        loadImage();
    }

    public void lastPage() throws IOException {
        index = imageList.size() - 1;
        loadImage();
    }
}
