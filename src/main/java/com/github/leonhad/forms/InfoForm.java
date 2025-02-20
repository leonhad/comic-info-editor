package com.github.leonhad.forms;

import com.github.leonhad.components.LevelBar;
import com.github.leonhad.document.Document;
import com.github.leonhad.utils.CodeValue;
import com.github.leonhad.utils.ISOLanguage;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class InfoForm extends JDialog {

    private final Document document;

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

    private final JComboBox<String> format = new JComboBox<>();

    private final JComboBox<String> ageRating = new JComboBox<>();

    private final JComboBox<CodeValue> blackAndWhite = new JComboBox<>();

    private final JComboBox<CodeValue> manga = new JComboBox<>();

    private final JTextField characters = new JTextField();

    private final JTextField teams = new JTextField();

    private final JTextField locations = new JTextField();

    private final JTextField scanInformation = new JTextField();

    private final LevelBar communityRating = new LevelBar();

    public InfoForm(Frame parent, Document document) {
        super(parent);
        this.document = document;

        setTitle("Edit document information");

        initFieldData();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createFieldPanel(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(600, 1));
        pack();

        setLocationRelativeTo(parent);
        setVisible(true);
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
        panel.add(summary, "span, grow, wrap");
        panel.add(new JLabel("Year:"));
        panel.add(year, "grow");
        panel.add(new JLabel("Month:"));
        panel.add(month, "grow, wrap");
        panel.add(new JLabel("Day:"));
        panel.add(day, "grow");
        panel.add(new JLabel("Language ISO:"));
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
        panel.add(communityRating, "wrap");

        return panel;
    }

    private void initFieldData() {
        format.addItem("Special");
        format.addItem("Reference");
        format.addItem("Director's Cut");
        format.addItem("Box Set");
        format.addItem("Box-Set");
        format.addItem("Annual");
        format.addItem("Anthology");
        format.addItem("Epilogue");
        format.addItem("One Shot");
        format.addItem("One-Shot");
        format.addItem("Prologue");
        format.addItem("TPB");
        format.addItem("Trade Paper Back");
        format.addItem("Omnibus");
        format.addItem("Compendium");
        format.addItem("Absolute");
        format.addItem("Graphic Novel");
        format.addItem("GN");
        format.addItem("FCBD");

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
