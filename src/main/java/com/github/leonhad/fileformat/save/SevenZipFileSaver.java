package com.github.leonhad.fileformat.save;

import com.github.leonhad.filefilters.Cb7Filter;
import com.github.leonhad.filefilters.SevenZipFilter;
import com.github.leonhad.fileformat.FileSaver;
import lombok.RequiredArgsConstructor;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class SevenZipFileSaver extends FileSaver {

    private RandomAccessFile randomFile;

    private IOutCreateArchive7z outArchive;

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new Cb7Filter(),
                new SevenZipFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        if (file.exists()) {
            this.randomFile = new RandomAccessFile(file, "rw");
            var stream = new RandomAccessFileInStream(randomFile);

            var a = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, stream);
            IOutUpdateArchive<IOutItemAllFormats> out = a.getConnectedOutArchive();
            //out.updateItems();
        } else {
            this.randomFile = new RandomAccessFile(file, "rw");
            this.outArchive = SevenZip.openOutArchive7z();

            // Configure archive
            this.outArchive.setLevel(5);
        }
    }

    @Override
    public void addEntries(List<EntrySaver> entries) throws IOException {
        outArchive.createArchive(new RandomAccessFileOutStream(this.randomFile), entries.size(), new WriteCallback(entries));
    }

    @Override
    public void close() throws Exception {
        this.outArchive.close();
        this.randomFile.close();
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
