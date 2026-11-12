package com.github.leonhad.fileformat.open;

import com.github.leonhad.document.ImageMetadata;
import com.github.leonhad.filefilters.*;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class SevenZipFileOpener extends FileOpener {

    private RandomAccessFile random;
    private RandomAccessFileInStream stream;
    private IInArchive archive;

    private List<ImageMetadata> imageList;

    private static ByteArrayInputStream getComicInfo(IInArchive archive) throws IOException {
        var simpleInArchive = archive.getSimpleInterface();

        for (var item : simpleInArchive.getArchiveItems()) {
            if (item.getPath().equals("ComicInfo.xml")) {
                var out = new ByteArrayOutputStream();
                item.extractSlow(data -> {
                    try {
                        out.write(data);
                        return data.length;
                    } catch (IOException e) {
                        throw new SevenZipException(e);
                    }
                });

                return new ByteArrayInputStream(out.toByteArray());
            }
        }

        throw new IOException("ComicInfo.xml not found.");
    }

    @Override
    protected FileFilter[] getFilters() {
        return new FileFilter[]{
                new RarFilter(),
                new SevenZipFilter(),
                new TarFilter(),
                new CbrFilter(),
                new Cb7Filter(),
                new CbtFilter()
        };
    }

    @Override
    public void open(File file) throws IOException {
        random = new RandomAccessFile(file, "r");
        stream = new RandomAccessFileInStream(random);
        archive = SevenZip.openInArchive(null, stream);

        imageList = getImageList(archive);
        if (imageList.isEmpty()) {
            throw new IOException("No image found");
        }
    }

    @Override
    public boolean getComicInfo(Predicate<InputStream> consumer) {
        try {
            return consumer.test(getComicInfo(archive));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<ImageMetadata> getImageList() {
        return imageList;
    }

    private static List<ImageMetadata> getImageList(IInArchive archive) throws SevenZipException {
        var imageList = new ArrayList<ImageMetadata>();
        var listItems = new ArrayList<Integer>();

        int itemCount = archive.getNumberOfItems();

        for (int i = 0; i < itemCount; i++) {
            var path = archive.getProperty(i, PropID.PATH).toString();

            if (!(boolean) archive.getProperty(i, PropID.IS_FOLDER) && FileFactory.isValidImage(path)) {
                listItems.add(i);
                imageList.add(new ImageMetadata(path));
            }
        }

        archive.extract(listItems.stream().mapToInt(Integer::intValue).toArray(), false, new ExtractCallback(archive, imageList));

        imageList.sort(Comparator.comparing(ImageMetadata::getFileName, String.CASE_INSENSITIVE_ORDER));
        return imageList;
    }

    @RequiredArgsConstructor
    private static class ExtractCallback implements IArchiveExtractCallback {

        private final IInArchive archive;

        private final ArrayList<ImageMetadata> imageList;

        private int currentIndex;

        // Map to accumulate chunks for each archive item
        private final Map<Integer, ByteArrayOutputStream> streamMap = new HashMap<>();

        @Override
        public void setTotal(long total) {
            // Not used.
        }

        @Override
        public void setCompleted(long complete) {
            // Not used.
        }

        @Override
        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) {
            if (extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }

            currentIndex = index;
            var out = new ByteArrayOutputStream();
            streamMap.put(index, out);
            return data -> {
                out.write(data, 0, data.length);
                return data.length;
            };
        }

        @Override
        public void prepareOperation(ExtractAskMode extractAskMode) {
            // Not used.
        }

        @Override
        public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            // After extraction completes, transfer accumulated data to ImageMetadata
            var out = streamMap.get(currentIndex);
            if (out != null) {
                var path = archive.getProperty(currentIndex, PropID.PATH).toString();
                imageList.stream()
                        .filter(i -> i.getFileName().equals(path))
                        .findAny()
                        .ifPresent(i -> i.setImage(out.toByteArray()));
            }
        }
    }

    @Override
    public void close() throws Exception {
        stream.close();
        random.close();
        archive.close();
    }
}
