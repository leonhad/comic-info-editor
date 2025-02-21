package com.github.leonhad.forms;

import com.github.leonhad.components.RatingComponent;
import com.github.leonhad.document.Metadata;
import com.github.leonhad.utils.CodeValue;
import com.github.leonhad.utils.ISOLanguage;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class InfoForm extends JDialog {

    private final Metadata metadata;

    private final JTextField title = new JTextField();

    private final JTextField series = new JTextField();

    private final JTextField number = new JTextField();

    private final JTextField count = new JTextField();

    private final JTextArea summary = new JTextArea();

    private final JTextField year = new JTextField();

    private final JTextField month = new JTextField();

    private final JTextField day = new JTextField();

    private final JTextField writer = new JTextField();

    private final JTextField penciller = new JTextField();

    private final JTextField inker = new JTextField();

    private final JTextField colorist = new JTextField();

    private final JTextField letterer = new JTextField();

    private final JTextField coverArtist = new JTextField();

    private final JTextField editor = new JTextField();

    private final JTextField translator = new JTextField();

    private final JTextField publisher = new JTextField();

    private final JTextField imprint = new JTextField();

    private final JTextField genre = new JTextField();

    private final JTextField tags = new JTextField();

    private final JTextField web = new JTextField();

    private final JComboBox<ISOLanguage> languageIso = new JComboBox<>();

    private final JTextField format = new JTextField();

    private final JComboBox<String> ageRating = new JComboBox<>();

    private final JComboBox<CodeValue> blackAndWhite = new JComboBox<>();

    private final JComboBox<CodeValue> manga = new JComboBox<>();

    private final JTextField characters = new JTextField();

    private final JTextField teams = new JTextField();

    private final JTextField locations = new JTextField();

    private final JTextField scanInformation = new JTextField();

    private final JTextField gtin = new JTextField();

    private final JTextField volume = new JTextField();

    private final RatingComponent communityRating = new RatingComponent();

    public InfoForm(Frame parent, Metadata metadata) {
        super(parent);
        setResizable(false);

        this.metadata = metadata;

        setTitle("Edit metadata");

        initFieldData();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createFieldPanel(), BorderLayout.CENTER);
        getContentPane().add(createOptionsPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(600, 1));

        setLocationRelativeTo(parent);

        loadMetadata();
        setVisible(true);

        pack();
    }

    private void loadMetadata() {
        title.setText(metadata.getTitle());
        series.setText(metadata.getSeries());
        number.setText(metadata.getNumber());
        count.setText(metadata.getCount());
        summary.setText(metadata.getSummary());
        year.setText(metadata.getYear());
        month.setText(metadata.getMonth());
        day.setText(metadata.getDay());
        writer.setText(metadata.getWriter());
        penciller.setText(metadata.getPenciller());
        inker.setText(metadata.getInker());
        colorist.setText(metadata.getColorist());
        letterer.setText(metadata.getLetterer());
        coverArtist.setText(metadata.getCoverArtist());
        editor.setText(metadata.getEditor());
        translator.setText(metadata.getTranslator());
        publisher.setText(metadata.getPublisher());
        imprint.setText(metadata.getImprint());
        genre.setText(metadata.getGenre());
        tags.setText(metadata.getTags());
        web.setText(metadata.getWeb());
        format.setText(metadata.getFormat());
        gtin.setText(metadata.getGtin());
        volume.setText(metadata.getVolume());
        characters.setText(metadata.getCharacters());
        teams.setText(metadata.getTeams());
        locations.setText(metadata.getLocations());
        scanInformation.setText(metadata.getScanInformation());
        communityRating.setText(metadata.getCommunityRating());

        selectItem(languageIso, v -> v.getIsoCode().equals(metadata.getLanguageIso()));
        selectItem(ageRating, v -> v.equals(metadata.getAgeRating()));
        selectItem(blackAndWhite, v -> v.getCode().equals(metadata.getBlackAndWhite()));
        selectItem(manga, v -> v.getCode().equals(metadata.getManga()));
    }

    private <T> void selectItem(JComboBox<T> item, Function<T, Boolean> equals) {
        for (int i = 0; i < item.getItemCount(); i++) {
            T t = item.getItemAt(i);
            if (equals.apply(t)) {
                item.setSelectedIndex(i);
                break;
            }
        }
    }

    private void confirm() {
        metadata.setTitle(title.getText());
        metadata.setSeries(series.getText());
        metadata.setNumber(number.getText());
        metadata.setCount(count.getText());
        metadata.setSummary(summary.getText());
        metadata.setYear(year.getText());
        metadata.setMonth(month.getText());
        metadata.setDay(day.getText());
        metadata.setWriter(writer.getText());
        metadata.setPenciller(penciller.getText());
        metadata.setInker(inker.getText());
        metadata.setColorist(colorist.getText());
        metadata.setLetterer(letterer.getText());
        metadata.setCoverArtist(coverArtist.getText());
        metadata.setEditor(editor.getText());
        metadata.setTranslator(translator.getText());
        metadata.setPublisher(publisher.getText());
        metadata.setImprint(imprint.getText());
        metadata.setGenre(genre.getText());
        metadata.setTags(tags.getText());
        metadata.setWeb(web.getText());
        metadata.setFormat(format.getText());
        metadata.setCharacters(characters.getText());
        metadata.setTeams(teams.getText());
        metadata.setLocations(locations.getText());
        metadata.setScanInformation(scanInformation.getText());
        metadata.setCommunityRating(communityRating.getText());
        metadata.setGtin(gtin.getText());
        metadata.setVolume(volume.getText());

        metadata.setAgeRating((String)ageRating.getSelectedItem());
        metadata.setLanguageIso(Optional.ofNullable((ISOLanguage) languageIso.getSelectedItem()).map(ISOLanguage::getIsoCode).orElse(null));
        metadata.setBlackAndWhite(Optional.ofNullable((CodeValue) blackAndWhite.getSelectedItem()).map(CodeValue::getCode).orElse(null));
        metadata.setManga(Optional.ofNullable((CodeValue) manga.getSelectedItem()).map(CodeValue::getCode).orElse(null));

        dispose();
    }

    private JPanel createOptionsPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout());

        var confirm = new JButton("Confirm");
        confirm.addActionListener(e -> confirm());
        confirm.setMnemonic('f');
        panel.add(confirm);

        var cancel = new JButton("Cancel");
        cancel.addActionListener(e -> this.dispose());
        cancel.setActionCommand("cancel");
        cancel.setMnemonic('c');
        panel.add(cancel);

        return panel;
    }

    private JPanel createFieldPanel() {
        summary.setLineWrap(true);
        summary.setRows(4);
        summary.setBorder(BorderFactory.createEtchedBorder());

        var panel = new JPanel(new MigLayout("fill", "[][grow][][grow]", ""));
        panel.add(new JLabel("Title:"));
        panel.add(title, "span, grow, wrap");
        panel.add(new JLabel("Series:"));
        panel.add(series, "span, grow, wrap");
        panel.add(new JLabel("Issue/Chapter number:"));
        panel.add(number, "grow");
        panel.add(new JLabel("Book count:"));
        panel.add(count, "grow, wrap");
        panel.add(new JLabel("Summary:"));
        panel.add(new JScrollPane(summary), "span, grow, wrap");
        panel.add(new JLabel("Volume:"));
        panel.add(volume, "grow, wrap");
        panel.add(new JLabel("Year:"));
        panel.add(year, "grow");
        panel.add(new JLabel("Month:"));
        panel.add(month, "grow, wrap");
        panel.add(new JLabel("Day:"));
        panel.add(day, "grow");
        panel.add(new JLabel("Language:"));
        panel.add(languageIso, "grow, wrap");
        panel.add(new JLabel("Writer:"));
        panel.add(writer, "span, grow, wrap");
        panel.add(new JLabel("Penciller:"));
        panel.add(penciller, "span, grow, wrap");
        panel.add(new JLabel("Inker:"));
        panel.add(inker, "span, grow, wrap");
        panel.add(new JLabel("Colorist:"));
        panel.add(colorist, "span, grow, wrap");
        panel.add(new JLabel("Letterer:"));
        panel.add(letterer, "span, grow, wrap");
        panel.add(new JLabel("Cover artist:"));
        panel.add(coverArtist, "span, grow, wrap");
        panel.add(new JLabel("Editor:"));
        panel.add(editor, "span, grow, wrap");
        panel.add(new JLabel("Translator:"));
        panel.add(translator, "span, grow, wrap");
        panel.add(new JLabel("Publisher:"));
        panel.add(publisher, "span, grow, wrap");
        panel.add(new JLabel("Imprint:"));
        panel.add(imprint, "span, grow, wrap");
        panel.add(new JLabel("Genre:"));
        panel.add(genre, "span, grow, wrap");
        panel.add(new JLabel("Tags:"));
        panel.add(tags, "span, grow, wrap");
        panel.add(new JLabel("Web:"));
        panel.add(web, "span, grow, wrap");
        panel.add(new JLabel("Characters:"));
        panel.add(characters, "span, grow, wrap");
        panel.add(new JLabel("Teams:"));
        panel.add(teams, "span, grow, wrap");
        panel.add(new JLabel("Locations:"));
        panel.add(locations, "span, grow, wrap");
        panel.add(new JLabel("Scan Information:"));
        panel.add(scanInformation, "span, grow, wrap");
        panel.add(new JLabel("Format:"));
        panel.add(format, "grow");
        panel.add(new JLabel("Age rating:"));
        panel.add(ageRating, "grow, wrap");
        panel.add(new JLabel("Black & white:"));
        panel.add(blackAndWhite, "grow");
        panel.add(new JLabel("Manga:"));
        panel.add(manga, "grow, wrap");
        panel.add(new JLabel("Community rating:"));
        panel.add(communityRating);
        panel.add(new JLabel("GTIN/ISBN:"));
        panel.add(gtin, "grow, wrap");

        return panel;
    }

    private void initFieldData() {
        ageRating.addItem("Unknown");
        ageRating.addItem("Rating Pending");
        ageRating.addItem("Early Childhood");
        ageRating.addItem("Everyone");
        ageRating.addItem("G");
        ageRating.addItem("Everyone 10+");
        ageRating.addItem("PG");
        ageRating.addItem("Kids to Adults");
        ageRating.addItem("Teen");
        ageRating.addItem("MA15+");
        ageRating.addItem("Mature 17+");
        ageRating.addItem("M");
        ageRating.addItem("R18+");
        ageRating.addItem("Adults Only 18+");
        ageRating.addItem("X18+");

        blackAndWhite.addItem(new CodeValue("Unknown", "Unknown"));
        blackAndWhite.addItem(new CodeValue("No", "No"));
        blackAndWhite.addItem(new CodeValue("Yes", "Yes"));

        manga.addItem(new CodeValue("Unknown", "Unknown"));
        manga.addItem(new CodeValue("No", "No"));
        manga.addItem(new CodeValue("Yes", "Yes"));
        manga.addItem(new CodeValue("YesAndRightToLeft", "Yes and right to left"));

        var items = ISOLanguage.list();
        items.forEach(languageIso::addItem);

        var locale = Locale.getDefault().toLanguageTag();
        languageIso.setSelectedItem(items.stream().filter(x -> x.getIsoCode().equals(locale)).findFirst().orElse(null));
    }
}
