package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbtFilter;
import com.github.leonhad.filefilters.TarFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import lombok.Getter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TarFileOpener extends FileOpener {

    @Getter
    private List<ImageMetadata> imageList;

    @Getter
    private byte[] comicInfo;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new TarFilter(),
                new CbtFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        imageList = new ArrayList<>();
        comicInfo = null;

        try (var fis = new FileInputStream(file);
             var bos = new BufferedInputStream(fis);
             var tarInputStream = new TarArchiveInputStream(bos)) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                var name = entry.getName();
                if (FileFactory.isValidImage(name)) {
                    var bytes = tarInputStream.readAllBytes();
                    imageList.add(new ImageMetadata(name, bytes));
                } else if ("ComicInfo.xml".equals(name)) {
                    comicInfo = tarInputStream.readAllBytes();
                } else {
                    // Skip other entries
                    tarInputStream.readAllBytes();
                }
            }
        }

        imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));

        if (imageList.isEmpty()) {
            throw new IOException("No image found");
        }
    }
}
