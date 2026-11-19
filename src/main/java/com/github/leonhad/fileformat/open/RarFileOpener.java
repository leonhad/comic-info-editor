package com.github.leonhad.fileformat.open;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.CbrFilter;
import com.github.leonhad.filefilters.RarFilter;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RarFileOpener extends FileOpener {

    @Getter
    private List<ImageMetadata> imageList;

    @Getter
    private byte[] comicInfo;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new RarFilter(),
                new CbrFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        try (var input = new FileInputStream(file);
             var buffer = new BufferedInputStream(input);
             var archive = new Archive(buffer)) {
            imageList = new ArrayList<>();
            comicInfo = null;

            while (true) {
                var fileHeader = archive.nextFileHeader();
                if (fileHeader == null) {
                    break;
                }

                var name = fileHeader.getFileName();
                var out = new ByteArrayOutputStream();
                archive.extractFile(fileHeader, out);

                if (FileFactory.isValidImage(name)) {
                    imageList.add(new ImageMetadata(name, out.toByteArray()));
                } else if ("ComicInfo.xml".equals(name)) {
                    comicInfo = out.toByteArray();
                }

            }

            imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));

            if (imageList.isEmpty()) {
                throw new IOException("No image found");
            }
        } catch (RarException e) {
            throw new IOException(e);
        }
    }

}
