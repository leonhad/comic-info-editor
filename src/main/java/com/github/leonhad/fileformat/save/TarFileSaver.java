package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.CbtFilter;
import com.github.leonhad.filefilters.TarFilter;
import com.github.leonhad.fileformat.FileSaver;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.util.List;

public class TarFileSaver extends FileSaver {

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new CbtFilter(),
                new TarFilter()
        };
    }

    @Override
    public void save(File file, List<EntrySaver> entries) throws IOException {
        try (var fos = new FileOutputStream(file);
             var bos = new BufferedOutputStream(fos);
             TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {

            for (var entry : entries) {
                try (var out = new ByteArrayOutputStream()) {
                    entry.getConsumer().accept(out);
                    var bytes = out.toByteArray();

                    var tar = new TarArchiveEntry(entry.getName());
                    tar.setName(entry.getName());
                    tar.setSize(bytes.length);

                    taos.putArchiveEntry(tar);
                    taos.write(bytes);

                } finally {
                    taos.closeArchiveEntry();
                }
            }
        }
    }
}
