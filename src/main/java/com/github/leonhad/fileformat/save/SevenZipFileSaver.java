package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.Cb7Filter;
import com.github.leonhad.filefilters.SevenZipFilter;
import com.github.leonhad.fileformat.FileSaver;
import lombok.RequiredArgsConstructor;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import java.io.*;
import java.util.HashMap;
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
        try (var randomFile = new RandomAccessFile(file, "rw");
             var out = new RandomAccessFileOutStream(randomFile);
             var outArchive = SevenZip.openOutArchive7z()) {

            // Configure archive
            outArchive.setLevel(9);

            outArchive.createArchive(out, entries.size(), new WriteCallback(entries));
        }
    }

    @RequiredArgsConstructor
    private static final class WriteCallback implements IOutCreateCallback<IOutItem7z> {

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

        public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) {
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
