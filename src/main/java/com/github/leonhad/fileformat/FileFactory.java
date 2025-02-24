package com.github.leonhad.fileformat;

import com.github.leonhad.fileformat.open.SevenZipFileOpener;
import com.github.leonhad.fileformat.open.ZipFileOpener;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileFactory {

    private static final List<FileOpener> FILE_OPENERS = List.of(new ZipFileOpener(), new SevenZipFileOpener());

    private static final List<String> IMAGE_EXTENSIONS = Stream.of(ImageIO.getReaderFileSuffixes())
            .map(String::toLowerCase).collect(Collectors.toList());

    public static FileOpener getOpener(File file) throws IOException {
        var opener = FILE_OPENERS.stream().filter(f -> f.canOpen(file))
                .findFirst()
                .orElseThrow(() -> new IOException("Unsupported file format."));
        opener.open(file);
        return opener;
    }

    public static boolean isValidImage(String fileName) {
        var lower = fileName.toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}
