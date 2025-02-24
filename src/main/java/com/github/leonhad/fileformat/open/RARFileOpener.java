package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbrFilter;
import com.github.leonhad.filefilters.RarFilter;
import com.github.leonhad.fileformat.FileOpener;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class RARFileOpener extends FileOpener {

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
        try (var random = new RandomAccessFile(file, "r");
             var stream = new RandomAccessFileInStream(random);
             var archive = SevenZip.openInArchive(null, stream)) {

            return ImageIO.read(getByteArrayInputStream(archive, image.getFileName()));
        }
    }

    private static ByteArrayInputStream getByteArrayInputStream(IInArchive archive, String fileName) throws IOException {

        var simpleInArchive = archive.getSimpleInterface();

        for (var item : simpleInArchive.getArchiveItems()) {
            if (item.getPath().equals(fileName)) {
                var out = new ByteArrayOutputStream();
                item.extractSlow(data -> {
                    try {
                        out.write(data);
                        return data.length;
                    } catch (IOException e) {
                        throw new SevenZipException(e);
                    }
                });

                return new ByteArrayInputStream(out.toByteArray());
            }
        }

        throw new IOException("File not found.");
    }

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new RarFilter(),
                new CbrFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        this.file = file;

        try (var random = new RandomAccessFile(file, "r");
             var stream = new RandomAccessFileInStream(random);
             var archive = SevenZip.openInArchive(null, stream)) {

            imageList = getImageList(archive);
            var image = imageList.stream().findFirst().orElseThrow(() -> new IOException("No image found"));
            currentImage = ImageIO.read(getByteArrayInputStream(archive, image.getFileName()));
        }
    }

    @Override
    public boolean getComicInfo(Function<InputStream, Boolean> consumer) {

        try (var random = new RandomAccessFile(file, "r");
             var stream = new RandomAccessFileInStream(random);
             var archive = SevenZip.openInArchive(null, stream)) {

            return consumer.apply(getByteArrayInputStream(archive, "ComicInfo.xml"));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<ImageMetadata> getImageList() {
        return imageList;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    private static List<ImageMetadata> getImageList(IInArchive archive) throws SevenZipException {
        var imageList = new ArrayList<ImageMetadata>();

        int itemCount = archive.getNumberOfItems();
        for (int i = 0; i < itemCount; i++) {
            var path = archive.getProperty(i, PropID.PATH).toString();
            if (!(Boolean) archive.getProperty(i, PropID.IS_FOLDER) && (path.endsWith(".jpg") || path.endsWith(".png"))) {
                imageList.add(new ImageMetadata(path, (Long) archive.getProperty(i, PropID.SIZE)));
            }
        }

        imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));
        return imageList;
    }
}
