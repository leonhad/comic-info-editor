package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.CbtFilter;
import com.github.leonhad.filefilters.TarFilter;
import com.github.leonhad.fileformat.FileSaver;
import lombok.RequiredArgsConstructor;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemTar;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import java.io.*;
import java.util.HashMap;
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
        try (var randomFile = new RandomAccessFile(file, "rw");
             var out = new RandomAccessFileOutStream(randomFile);
             var outArchive = SevenZip.openOutArchiveTar()) {

            outArchive.createArchive(out, entries.size(), new WriteCallback(entries));
        }
    }

    @RequiredArgsConstructor
    private static final class WriteCallback implements IOutCreateCallback<IOutItemTar> {

        private final List<EntrySaver> entries;

        private final HashMap<Integer, byte[]> files = new HashMap<>();

        public void setOperationResult(boolean operationResultOk) {
            // Track each operation result here
        }

        public void setTotal(long total) {
            // Track operation progress here
        }

        public void setCompleted(long complete) {
            // Track operation progress here
        }

        public IOutItemTar getItemInformation(int index, OutItemFactory<IOutItemTar> outItemFactory) {
            var item = outItemFactory.createOutItem();

            item.setPropertyPath(entries.get(index).getName());

            try (var out = new ByteArrayOutputStream()) {
                entries.get(index).getConsumer().accept(out);
                var file = out.toByteArray();
                files.put(index, file);
                item.setDataSize((long) file.length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return item;
        }

        public ISequentialInStream getStream(int i) {
            var out = new ByteArrayStream(files.get(i), true);
            files.remove(i);
            return out;
        }
    }
}
