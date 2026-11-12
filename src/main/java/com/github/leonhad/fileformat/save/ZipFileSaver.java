package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.fileformat.FileSaver;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileSaver extends FileSaver {

    private FileOutputStream output;
    private BufferedOutputStream buffer;
    private ZipOutputStream file;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new ZipFilter(),
                new CbzFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        this.output = new FileOutputStream(file);
        this.buffer = new BufferedOutputStream(output);
        this.file = new ZipOutputStream(buffer);

        this.file.setMethod(ZipOutputStream.DEFLATED);
        this.file.setLevel(9);
    }

    @Override
    public void addEntries(List<EntrySaver> entries) throws IOException {
        for (var entry : entries) {
            try {
                this.file.putNextEntry(new ZipEntry(entry.getName()));
                entry.getConsumer().accept(this.file);
            } finally {
                this.file.closeEntry();
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.file.close();
        this.buffer.close();
        this.output.close();
    }
}
