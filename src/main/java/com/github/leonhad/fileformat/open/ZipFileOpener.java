package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

public class ZipFileOpener extends FileOpener {

    private ZipFile zipFile;
    private List<ImageMetadata> imageList;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new ZipFilter(),
                new CbzFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        zipFile = new ZipFile(file);

        imageList = getImageList(zipFile);
        if (imageList.isEmpty()) {
            throw new IOException("No image found");
        }
    }

    @Override
    public boolean getComicInfo(Predicate<InputStream> consumer) {
        try {
            var info = zipFile.getEntry("ComicInfo.xml");
            if (info != null) {
                try (var input = zipFile.getInputStream(info)) {
                    return consumer.test(input);
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

    private static List<ImageMetadata> getImageList(ZipFile zipFile) throws IOException {
        var imageList = new ArrayList<ImageMetadata>();
        var i = zipFile.entries().asIterator();
        while (i.hasNext()) {
            var entry = i.next();
            var name = entry.getName();
            if (FileFactory.isValidImage(name)) {
                var input = zipFile.getInputStream(entry);
                var bytes = input.readAllBytes();

                imageList.add(new ImageMetadata(name, bytes));
            }
        }

        imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));
        return imageList;
    }

    @Override
    public void close() throws Exception {
        zipFile.close();
    }
}
