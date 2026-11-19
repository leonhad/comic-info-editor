package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.Cb7Filter;
import com.github.leonhad.filefilters.SevenZipFilter;
import com.github.leonhad.fileformat.FileSaver;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.*;
import java.util.List;

public class SevenZipFileSaver extends FileSaver {

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new Cb7Filter(),
                new SevenZipFilter()
        };
    }

    @Override
    public void save(File file, List<EntrySaver> entries) throws IOException {
        try (var sevenZFile = new SevenZOutputFile(file)) {

            for (var entry : entries) {
                try (var out = new ByteArrayOutputStream()) {
                    entry.getConsumer().accept(out);
                    var bytes = out.toByteArray();

                    var sevenZEntry = new SevenZArchiveEntry();
                    sevenZEntry.setName(entry.getName());
                    sevenZEntry.setSize(bytes.length);

                    sevenZFile.putArchiveEntry(sevenZEntry);
                    sevenZFile.write(bytes);
                } finally {
                    sevenZFile.closeArchiveEntry();
                }
            }
        }
    }
}
