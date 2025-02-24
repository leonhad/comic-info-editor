package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipFile;

public class ZipFileOpener extends FileOpener {

    private File file;
    private BufferedImage currentImage;
    private List<ImageMetadata> imageList;

    @Override
    public int getImageCount() {
        return imageList.size();
    }

    @Override
    protected void loadImage() throws IOException {
        this.currentImage = readImage(imageList.get(getIndex()));
    }

    @Override
    public BufferedImage readImage(ImageMetadata image) throws IOException {
        try (var zipFile = new ZipFile(file);
             var input = zipFile.getInputStream(zipFile.getEntry(image.getFileName()));
             var buffer = new BufferedInputStream(input)) {
            return ImageIO.read(buffer);
        }
    }

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new ZipFilter(),
                new CbzFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        this.file = file;

        try (var zipFile = new ZipFile(file)) {
            imageList = getImageList(zipFile);
            var image = imageList.stream().findFirst().orElseThrow(() -> new IOException("No image found"));
            currentImage = loadImage(zipFile.getInputStream(zipFile.getEntry(image.getFileName())));
        }
    }

    @Override
    public boolean getComicInfo(Function<InputStream, Boolean> consumer) {
        try (var zipFile = new ZipFile(file)) {
            var info = zipFile.getEntry("ComicInfo.xml");
            if (info != null) {
                try (var input = zipFile.getInputStream(info)) {
                    return consumer.apply(input);
                }
            }
        } catch (IOException e) {
            // Not used.
        }

        return false;
    }

    @Override
    public List<ImageMetadata> getImageList() {
        return imageList;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    private static List<ImageMetadata> getImageList(ZipFile zipFile) {
        var imageList = new ArrayList<ImageMetadata>();
        zipFile.stream().forEach(entry -> {
            var name = entry.getName();
            if (FileFactory.isValidImage(name)) {
                imageList.add(new ImageMetadata(name, entry.getSize()));
            }
        });

        imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));
        return imageList;
    }
}
