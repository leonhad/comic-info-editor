package com.github.leonhad.document;

import com.github.leonhad.utils.StatusBar;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Document {

    private Metadata metadata;
    private final List<PageMetadata> pagesMetadata = new ArrayList<>();
    private final File file;
    private boolean fileLoaded = false;
    private int index;
    private BufferedImage currentImage;

    private final List<ZipEntry> xmlList = new ArrayList<>();

    public Document(File file) throws IOException {
        StatusBar.setStatus("Loading file...");
        this.file = file;

        var imageList = new ArrayList<ZipEntry>();

        try (var zipFile = new ZipFile(file)) {
            zipFile.stream().forEach(entry -> {
                var name = entry.getName();
                if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    imageList.add(entry);
                } else if (name.endsWith(".xml")) {
                    xmlList.add(entry);
                }
            });

            imageList.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));
            xmlList.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));

            imageList.stream().findFirst().orElseThrow(() -> new IOException("No image found"));
            StatusBar.setFileStatus(imageList.size() + " images");

            loadImage(zipFile.getInputStream(imageList.get(0)));

            var info = zipFile.getEntry("ComicInfo.xml");
            if (info != null) {
                loadMetadata();

                if (pagesMetadata.size() != imageList.size()) {
                    rebuildMetadata(imageList);
                } else {
                    StatusBar.setImageStatus(pagesMetadata.get(index).getType().getDescription());
                    fileLoaded = true;
                    StatusBar.setStatus("File loaded.");
                }
            } else {
                rebuildMetadata(imageList);
            }
        } catch (IOException e) {
            StatusBar.setStatus("Error loading file.");
            throw e;
        }
    }

    private void rebuildMetadata(List<ZipEntry> imageList) {
        this.metadata = new Metadata();
        StatusBar.setStatus("Processing page metadata...");
        new Thread(() -> processPageMetadata(imageList)).start();
    }

    private void processPageMetadata(List<ZipEntry> imageList) {
        try (var zipFile = new ZipFile(file)) {
            for (int i = 0; i < imageList.size(); i++) {
                var entry = imageList.get(i);
                try (var stream = zipFile.getInputStream(entry);
                     var buffer = new BufferedInputStream(stream)) {
                    var image = ImageIO.read(buffer);

                    pagesMetadata.add(new PageMetadata(entry, i, image.getWidth(), image.getHeight()));
                }

                if (i == 0) {
                    StatusBar.setImageStatus(pagesMetadata.get(0).getType().getDescription());
                }
            }

            fileLoaded = true;
            StatusBar.setStatus("File loaded.");
        } catch (IOException ex) {
            fileLoaded = false;
            StatusBar.setStatus("Error loading file.");
        }
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public int getWidth() {
        return currentImage.getWidth();
    }

    public int getHeight() {
        return currentImage.getHeight();
    }

    private void loadMetadata() {
        this.metadata = new Metadata();

        // Load from file
    }

    public void save() throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        try (var fs = FileSystems.newFileSystem(file.toPath(), null)) {
            Path source = fs.getPath("/ComicInfo.xml");

            try (var out = Files.newOutputStream(source)) {
                writeXml(out);
            }
        }
    }

    public void saveAs(Path path) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        // FIXME copy original images

        try (var fs = FileSystems.newFileSystem(path, null)) {
            Path source = fs.getPath("/ComicInfo.xml");

            try (var out = Files.newOutputStream(source)) {
                writeXml(out);
            }
        }
    }

    private void writeXml(OutputStream out) throws IOException {
        var document = new org.jdom2.Document();
        var root = new Element("ComicInfo");

        addContent(root, "Title", metadata.getTitle());
        addContent(root, "Series", metadata.getSeries());
        addContent(root, "Number", metadata.getNumber());
        addContent(root, "Count", metadata.getCount());
        addContent(root, "Summary", metadata.getSummary());
        addContent(root, "Year", metadata.getYear());
        addContent(root, "Month", metadata.getMonth());
        addContent(root, "Day", metadata.getDay());
        addContent(root, "Writer", metadata.getWriter());
        addContent(root, "Penciller", metadata.getPenciller());
        addContent(root, "Inker", metadata.getInker());
        addContent(root, "Colorist", metadata.getColorist());
        addContent(root, "Letterer", metadata.getLetterer());
        addContent(root, "CoverArtist", metadata.getCoverArtist());
        addContent(root, "Editor", metadata.getEditor());
        addContent(root, "Translator", metadata.getTranslator());
        addContent(root, "Publisher", metadata.getPublisher());
        addContent(root, "Imprint", metadata.getImprint());
        addContent(root, "Genre", metadata.getGenre());
        addContent(root, "Tags", metadata.getTags());
        addContent(root, "Web", metadata.getWeb());
        addContent(root, "LanguageISO", metadata.getLanguageIso());
        addContent(root, "Format", metadata.getFormat());
        addContent(root, "AgeRating", metadata.getAgeRating());
        addContent(root, "BlackAndWhite", metadata.getBlackAndWhite());
        addContent(root, "Manga", metadata.getManga());
        addContent(root, "Characters", metadata.getCharacters());
        addContent(root, "Teams", metadata.getTeams());
        addContent(root, "Locations", metadata.getLocations());
        addContent(root, "ScanInformation", metadata.getScanInformation());
        addContent(root, "CommunityRating", metadata.getCommunityRating());
        addContent(root, "PageCount", Integer.toString(pagesMetadata.size()));

        var pages = new Element("Pages");
        for (var pageMetadata : this.pagesMetadata) {
            var page = new Element("Page");
            page.setAttribute(new Attribute("Image", Integer.toString(pageMetadata.getPageNumber())));
            page.setAttribute(new Attribute("ImageSize", Long.toString(pageMetadata.getSize())));
            page.setAttribute(new Attribute("ImageWidth", Integer.toString(pageMetadata.getWidth())));
            page.setAttribute(new Attribute("ImageHeight", Integer.toString(pageMetadata.getHeight())));
            Optional.ofNullable(pageMetadata.getType()).ifPresent(type -> page.setAttribute(new Attribute("Type", type.getText())));
            pages.addContent(page);
        }

        root.addContent(pages);
        document.setContent(root);

        try {
            var transformerFactory = TransformerFactory.newInstance();
            var transformer = transformerFactory.newTransformer();
            var source = new JDOMSource(document);
            var result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private void addContent(Element element, String tag, String value) {
        if (value != null && !value.isEmpty()) {
            element.addContent(createElement(tag, metadata.getSeries()));
        }
    }

    private Element createElement(String tag, String value) {
        var element = new Element(tag);
        element.setText(value);
        return element;
    }

    private void loadImage(InputStream inputStream) throws IOException {
        try (var buffer = new BufferedInputStream(inputStream)) {
            this.currentImage = ImageIO.read(buffer);

            if(fileLoaded) {
                StatusBar.setImageStatus(pagesMetadata.get(index).getType().getDescription());
            }
        }
    }

    private void loadImage() throws IOException {
        try (var zipFile = new ZipFile(file);
             var stream = pagesMetadata.get(index).createInputStream(zipFile)) {
            loadImage(stream);
        }
    }

    public void nextPage() throws IOException {
        index++;
        if (index >= pagesMetadata.size()) {
            index = pagesMetadata.size() - 1;
        }

        loadImage();
    }

    public void previousPage() throws IOException {
        index--;
        if (index < 0) {
            index = 0;
        }

        loadImage();
    }

    public void firstPage() throws IOException {
        index = 0;
        loadImage();
    }

    public void lastPage() throws IOException {
        index = pagesMetadata.size() - 1;
        loadImage();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setPageType(PageType pageType) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        pagesMetadata.get(index).setType(pageType);
        StatusBar.setImageStatus(Optional.ofNullable(pagesMetadata.get(index).getType()).map(PageType::getDescription).orElse(""));
    }
}
