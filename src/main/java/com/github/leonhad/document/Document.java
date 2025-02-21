package com.github.leonhad.document;

import com.github.leonhad.utils.StatusBar;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Document {

    private Metadata metadata;
    private final List<PageMetadata> pagesMetadata = new ArrayList<>();
    private final File file;
    private boolean fileLoaded = false;
    private int index;
    private BufferedImage currentImage;
    private final int imageCount;

    public Document(File file) throws IOException {
        StatusBar.setStatus("Loading file...");
        this.file = file;

        try (var zipFile = new ZipFile(file)) {
            var imageList = getImageList(zipFile);

            imageList.stream().findFirst().orElseThrow(() -> new IOException("No image found"));
            imageCount = imageList.size();
            StatusBar.setFileStatus(imageCount + " images");

            loadImage(zipFile.getInputStream(imageList.get(0)));

            var info = zipFile.getEntry("ComicInfo.xml");
            if (info != null) {
                loadMetadata(file);

                StatusBar.setStatus("File loaded.");
            } else {
                this.metadata = new Metadata();
                rebuildMetadata(imageList);
            }
        } catch (IOException e) {
            StatusBar.setStatus("Error loading file.");
            throw e;
        }
    }

    private static ArrayList<ZipEntry> getImageList(ZipFile zipFile) {
        var imageList = new ArrayList<ZipEntry>();
        zipFile.stream().forEach(entry -> {
            var name = entry.getName();
            if (name.endsWith(".jpg") || name.endsWith(".png")) {
                imageList.add(entry);
            }
        });

        imageList.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));
        return imageList;
    }

    private void rebuildMetadata(List<ZipEntry> imageList) {
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

    private void loadMetadata(File path) throws IOException {
        this.metadata = new Metadata();

        try (var zipFile = new ZipFile(path)) {
            var info = zipFile.getEntry("ComicInfo.xml");
            if (info == null) {
                return;
            }

            var factory = DocumentBuilderFactory.newInstance();
            var documentBuilder = factory.newDocumentBuilder();
            org.jdom2.Document document;
            try (var stream = zipFile.getInputStream(info);
                 var buffer = new BufferedInputStream(stream)) {
                document = new DOMBuilder().build(documentBuilder.parse(buffer));
            }

            var root = document.getRootElement();
            metadata.setTitle(root.getChildText("Title"));
            metadata.setSeries(root.getChildText("Series"));
            metadata.setNumber(root.getChildText("Number"));
            metadata.setCount(root.getChildText("Count"));
            metadata.setSummary(root.getChildText("Summary"));
            metadata.setVolume(root.getChildText("Volume"));
            metadata.setYear(root.getChildText("Year"));
            metadata.setMonth(root.getChildText("Month"));
            metadata.setDay(root.getChildText("Day"));
            metadata.setWriter(root.getChildText("Writer"));
            metadata.setPenciller(root.getChildText("Penciller"));
            metadata.setInker(root.getChildText("Inker"));
            metadata.setColorist(root.getChildText("Colorist"));
            metadata.setLetterer(root.getChildText("Letterer"));
            metadata.setCoverArtist(root.getChildText("CoverArtist"));
            metadata.setEditor(root.getChildText("Editor"));
            metadata.setTranslator(root.getChildText("Translator"));
            metadata.setPublisher(root.getChildText("Publisher"));
            metadata.setImprint(root.getChildText("Imprint"));
            metadata.setGenre(root.getChildText("Genre"));
            metadata.setTags(root.getChildText("Tags"));
            metadata.setWeb(root.getChildText("Web"));
            metadata.setLanguageIso(root.getChildText("LanguageISO"));
            metadata.setFormat(root.getChildText("Format"));
            metadata.setAgeRating(root.getChildText("AgeRating"));
            metadata.setBlackAndWhite(root.getChildText("BlackAndWhite"));
            metadata.setManga(root.getChildText("Manga"));
            metadata.setCharacters(root.getChildText("Characters"));
            metadata.setTeams(root.getChildText("Teams"));
            metadata.setLocations(root.getChildText("Locations"));
            metadata.setScanInformation(root.getChildText("ScanInformation"));
            metadata.setCommunityRating(root.getChildText("CommunityRating"));
            metadata.setGtin(root.getChildText("GTIN"));

            var imageList = getImageList(zipFile);
            pagesMetadata.clear();
            var pages = root.getChild("Pages");
            if (pages != null) {
                try {
                    for (var page : pages.getChildren()) {
                        var image = Integer.parseInt(page.getAttributeValue("Image"));
                        var type = PageType.fromText(page.getAttributeValue("Type"));
                        var imageWidth = Integer.parseInt(page.getAttributeValue("ImageWidth"));
                        var imageHeight = Integer.parseInt(page.getAttributeValue("ImageHeight"));

                        pagesMetadata.add(new PageMetadata(imageList.get(image), image, imageWidth, imageHeight, type));
                    }
                } catch (Exception ex) {
                    StatusBar.setImageStatus("Error parsing XML.");
                    rebuildMetadata(imageList);
                }
            }

            if (pagesMetadata.size() != imageCount) {
                rebuildMetadata(imageList);
            } else {
                StatusBar.setImageStatus(pagesMetadata.get(index).getType().getDescription());
                fileLoaded = true;
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
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

        StatusBar.setStatus("File saved.");
    }

    public void saveAs(Path path) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        if (!path.toFile().equals(file)) {
            StatusBar.setStatus("Saving file...");

            new Thread(() -> {
                try (var output = new FileOutputStream(path.toFile());
                     var bufferOut = new BufferedOutputStream(output);
                     var zipOut = new ZipOutputStream(bufferOut);
                     var input = new FileInputStream(file);
                     var bufferIn = new BufferedInputStream(input);
                     var zipIn = new ZipInputStream(bufferIn)) {

                    zipOut.setMethod(ZipOutputStream.DEFLATED);
                    zipOut.setLevel(9);

                    zipOut.putNextEntry(new ZipEntry("ComicInfo.xml"));
                    writeXml(zipOut);

                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (entry.getName().equals("ComicInfo.xml")) {
                            continue;
                        }

                        zipOut.putNextEntry(new ZipEntry(entry.getName()));
                        zipIn.transferTo(zipOut);
                    }
                    StatusBar.setStatus("File saved.");
                } catch (Exception e) {
                    StatusBar.setStatus("Unexpected error saving file.");
                }

            }).start();
        } else {
            save();
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
        addContent(root, "Volume", metadata.getVolume());
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
        addContent(root, "GTIN", metadata.getGtin());
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
            element.addContent(createElement(tag, value));
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
