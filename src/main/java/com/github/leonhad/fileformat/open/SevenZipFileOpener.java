package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.Cb7Filter;
import com.github.leonhad.filefilters.SevenZipFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import lombok.Getter;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SevenZipFileOpener extends FileOpener {

    @Getter
    private List<ImageMetadata> imageList;

    @Getter
    private byte[] comicInfo;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new SevenZipFilter(),
                new Cb7Filter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        try (var sevenZFile = SevenZFile.builder().setFile(file).get()) {
            imageList = new ArrayList<>();
            comicInfo = null;

            for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
                var name = entry.getName();
                if (FileFactory.isValidImage(name)) {
                    try (var input = sevenZFile.getInputStream(entry)) {
                        var bytes = input.readAllBytes();
                        imageList.add(new ImageMetadata(name, bytes));
                    }
                } else if ("ComicInfo.xml".equals(name)) {
                    try (var input = sevenZFile.getInputStream(entry)) {
                        comicInfo = input.readAllBytes();
                    }
                }
            }

            imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));

            if (imageList.isEmpty()) {
                throw new IOException("No image found");
            }
        }
    }

}
