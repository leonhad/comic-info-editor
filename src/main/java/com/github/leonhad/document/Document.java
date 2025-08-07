package com.github.leonhad.document;

import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.FileOpener;
import com.github.leonhad.utils.StatusBar;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Document {

    public static final String FILE_LOADED = "File loaded.";
    public static final String ERROR_LOADING_FILE = "Error loading file.";
    private Metadata metadata;
    private final List<PageMetadata> pagesMetadata = new ArrayList<>();
    private final File file;
    private boolean fileLoaded = false;
    private BufferedImage currentImage;
    private final FileOpener fileOpener;

    public Document(File file) throws IOException {
        StatusBar.setStatus("Loading file...");
        try {
            this.file = file;
            this.fileOpener = FileFactory.getOpener(file);
            this.currentImage = fileOpener.getCurrentImage();
            StatusBar.setFileStatus(fileOpener.getImageCount() + " images");

            var info = fileOpener.getComicInfo(inputStream -> {
                try {
                    loadMetadata(inputStream);
                    StatusBar.setStatus(FILE_LOADED);
                } catch (IOException e) {
                    StatusBar.setStatus(ERROR_LOADING_FILE);
                    return false;
                }

                return true;
            });

            if (!info) {
                this.metadata = new Metadata();
                rebuildMetadata();
            } else {
                fileLoaded = true;
                StatusBar.setStatus(FILE_LOADED);
            }
        } catch (IOException e) {
            StatusBar.setStatus(ERROR_LOADING_FILE);
            throw e;
        }
    }

    private void rebuildMetadata() {
        StatusBar.setStatus("Processing page metadata...");
        new Thread(this::processPageMetadata).start();
    }

    private void processPageMetadata() {
        try {
            for (int loop = 0; loop < fileOpener.getImageCount(); loop++) {
                var image = fileOpener.getImageList().get(loop);
                BufferedImage buffer = fileOpener.readImage(image);
                pagesMetadata.add(new PageMetadata(loop, image.getSize(), buffer.getWidth(), buffer.getHeight()));

                if (loop == 0) {
                    StatusBar.setImageStatus(pagesMetadata.get(0).getType().getDescription());
                }
            }

            fileLoaded = true;
            StatusBar.setStatus(FILE_LOADED);
        } catch (IOException ex) {
            fileLoaded = false;
            StatusBar.setStatus(ERROR_LOADING_FILE);
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

    private void loadMetadata(InputStream inputStream) throws IOException {
        this.metadata = new Metadata();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);

            var documentBuilder = factory.newDocumentBuilder();
            org.jdom2.Document document;
            try (var buffer = new BufferedInputStream(inputStream)) {
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

            pagesMetadata.clear();
            var imageList = fileOpener.getImageList();
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
                    rebuildMetadata();
                }
            }

            if (pagesMetadata.size() != imageList.size()) {
                rebuildMetadata();
            } else {
                StatusBar.setImageStatus(pagesMetadata.get(fileOpener.getIndex()).getType().getDescription());
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

        try (var fs = FileSystems.newFileSystem(file.toPath(), (ClassLoader) null)) {
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
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLInputFactory.SUPPORT_DTD, false);

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

    private void updateStatus() {
        if(fileLoaded) {
            StatusBar.setImageStatus(pagesMetadata.get(fileOpener.getIndex()).getType().getDescription());
        }
    }

    public void nextPage() throws IOException {
        this.currentImage = fileOpener.nextPage();
        updateStatus();
    }

    public void previousPage() throws IOException {
        this.currentImage = fileOpener.previousPage();
        updateStatus();
    }

    public void firstPage() throws IOException {
        this.currentImage = fileOpener.firstPage();
        updateStatus();
    }

    public void lastPage() throws IOException {
        this.currentImage = fileOpener.lastPage();
        updateStatus();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setPageType(PageType pageType) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        pagesMetadata.get(fileOpener.getIndex()).setType(pageType);
        StatusBar.setImageStatus(Optional.ofNullable(pagesMetadata.get(fileOpener.getIndex()).getType()).map(PageType::getDescription).orElse(""));
    }

    public File getFile() {
        return file;
    }
}
