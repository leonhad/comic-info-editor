package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import lombok.Getter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

public class ZipFileOpener extends FileOpener {

    @Getter
    private byte[] comicInfo;

    @Getter
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
        var zipFile = new ZipFile(file);

        imageList = getImageList(zipFile);
        comicInfo = null;

        if (imageList.isEmpty()) {
            throw new IOException("No image found");
        }

        try {
            var info = zipFile.getEntry("ComicInfo.xml");
            if (info != null) {
                try (var input = zipFile.getInputStream(info)) {
                    comicInfo = input.readAllBytes();
                }
            }
        } catch (IOException e) {
            // Not used.
        }
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
}
