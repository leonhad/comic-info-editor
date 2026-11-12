package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.fileformat.FileSaver;

import java.io.*;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileSaver extends FileSaver {

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new ZipFilter(),
                new CbzFilter()
        };
    }

    @Override
    public void save(File file, List<EntrySaver> entries) throws IOException {
        try (var output = new FileOutputStream(file);
             var buffer = new BufferedOutputStream(output);
             var zipFile = new ZipOutputStream(buffer)) {

            zipFile.setLevel(Deflater.DEFAULT_COMPRESSION);
            zipFile.setMethod(ZipOutputStream.DEFLATED);

            for (var entry : entries) {
                zipFile.putNextEntry(new ZipEntry(entry.getName()));
                entry.getConsumer().accept(zipFile);
                zipFile.closeEntry();
            }
        }
    }
}
