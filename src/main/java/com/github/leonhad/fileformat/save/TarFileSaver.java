package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.CbtFilter;
import com.github.leonhad.filefilters.TarFilter;
import com.github.leonhad.fileformat.FileSaver;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarOutputStream;

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
             var tar = new TarOutputStream(bos)) {

            for (var entry : entries) {
                var buffer = new ByteArrayOutputStream();
                entry.getConsumer().accept(buffer);

                var header = TarHeader.createHeader(entry.getName(), buffer.size(), System.currentTimeMillis(), false, 288);
                tar.putNextEntry(new TarEntry(header));
                tar.write(buffer.toByteArray());

                tar.flush();
            }
        }
    }
}
