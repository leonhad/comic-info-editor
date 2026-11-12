package com.github.leonhad.document;

import com.github.leonhad.components.StatusBar;
import com.github.leonhad.fileformat.FileFactory;
import com.github.leonhad.fileformat.save.EntrySaver;
import lombok.Getter;
import lombok.extern.java.Log;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Log
public class Document {

    public static final String FILE_LOADED = "File loaded.";
    public static final String ERROR_LOADING_FILE = "Error loading file.";
    public static final String COMIC_INFO_XML = "ComicInfo.xml";
    public static final String UNEXPECTED_ERROR_SAVING_FILE = "Unexpected error while saving file.";

    private final List<PageMetadata> pagesMetadata = new ArrayList<>();

    private final List<ImageMetadata> imageList;

    private boolean fileLoaded = false;

    private int index;

    @Getter
    private final File file;

    @Getter
    private Metadata metadata;

    public Document(File file) throws IOException {
        StatusBar.setStatus("Loading file...");
        this.file = file;

        try (var fileOpener = FileFactory.getOpener(file)) {
            this.imageList = fileOpener.getImageList();

            StatusBar.setFileStatus(imageList.size() + " images");

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
        } catch (Exception e) {
            log.log(Level.SEVERE, UNEXPECTED_ERROR_SAVING_FILE, e);
            StatusBar.setStatus(ERROR_LOADING_FILE);
            throw new IOException(e);
        }
    }

    private void rebuildMetadata() {
        StatusBar.setStatus("Processing page metadata...");
        new Thread(this::processPageMetadata).start();
    }

    private void processPageMetadata() {
        try {
            for (int loop = 0; loop < imageList.size(); loop++) {
                var image = imageList.get(loop);

                pagesMetadata.add(new PageMetadata(loop, imageList.size(), image.getWidth(), image.getHeight()));

                if (loop == 0) {
                    StatusBar.setImageStatus(pagesMetadata.get(0).getType().getDescription());
                }
            }

            fileLoaded = true;
            StatusBar.setStatus(FILE_LOADED);
        } catch (Exception e) {
            fileLoaded = false;
            StatusBar.setStatus(ERROR_LOADING_FILE);
            throw new RuntimeException(e);
        }
    }

    public int getWidth() {
        return imageList.get(index).getWidth();
    }

    public int getHeight() {
        return imageList.get(index).getHeight();
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

            var pages = root.getChild("Pages");
            if (pages != null) {
                processMetadata(pages, imageList);
            }

            if (pagesMetadata.size() != imageList.size()) {
                rebuildMetadata();
            } else {
                StatusBar.setImageStatus(pagesMetadata.get(index).getType().getDescription());
                fileLoaded = true;
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }

    private void processMetadata(Element pages, List<ImageMetadata> imageList) {
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

    public CompletableFuture<Void> save() throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        return saveAs(file.toPath());
    }

    public CompletableFuture<Void> saveAs(Path path) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        StatusBar.setStatus("Saving file...");

        var fileSaver = FileFactory.getWriter(path.toFile());

        var entries = new ArrayList<EntrySaver>();
        entries.add(new EntrySaver(COMIC_INFO_XML, this::writeXml));

        for (var image : imageList) {
            try (var is = new ByteArrayInputStream(image.getImage())) {
                entries.add(new EntrySaver(image.getFileName(), is::transferTo));
            }
        }

        return CompletableFuture.runAsync(() -> {
            try {
                fileSaver.save(path.toFile(), entries);

                StatusBar.setStatus("File saved.");
            } catch (Exception e) {
                log.log(Level.SEVERE, UNEXPECTED_ERROR_SAVING_FILE, e);
                StatusBar.setStatus(UNEXPECTED_ERROR_SAVING_FILE);
            }
        });
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
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

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
        if (fileLoaded) {
            StatusBar.setImageStatus(pagesMetadata.get(index).getType().getDescription());
        }
    }

    public void nextPage() {
        index++;
        if (index >= imageList.size()) {
            index = imageList.size() - 1;
        }

        updateStatus();
    }

    public void previousPage() {
        index--;
        if (index < 0) {
            index = 0;
        }

        updateStatus();
    }

    public void firstPage() {
        index = 0;

        updateStatus();
    }

    public void lastPage() {
        index = imageList.size() - 1;

        updateStatus();
    }

    public void setPageType(PageType pageType) throws IOException {
        if (!fileLoaded) {
            throw new IOException("File not loaded yet.");
        }

        pagesMetadata.get(index).setType(pageType);
        StatusBar.setImageStatus(Optional.ofNullable(pagesMetadata.get(index).getType()).map(PageType::getDescription).orElse(""));
    }

    public BufferedImage getCurrentImage() {
        return imageList.get(index).getBuffer();
    }
}
