package com.github.leonhad.fileformat;

import com.github.leonhad.fileformat.open.RARFileOpener;
import com.github.leonhad.fileformat.open.ZipFileOpener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class FileFactory {

    private static final List<FileOpener> FILE_OPENERS = List.of(new ZipFileOpener(), new RARFileOpener());

    public static FileOpener getOpener(File file) throws IOException {
        var opener = FILE_OPENERS.stream().filter(f -> f.canOpen(file))
                .findFirst()
                .orElseThrow(() -> new IOException("Unsupported file format."));
        opener.open(file);
        return opener;
    }
}
