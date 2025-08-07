package com.github.leonhad.forms;

import com.github.leonhad.components.MangaCellRenderer;
import com.github.leonhad.integration.ImageCache;
import com.github.leonhad.integration.Manga;
import com.github.leonhad.integration.anilist.AniList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SearchForm extends JDialog {

    private final JTextField searchField = new JTextField("");
    private final JButton searchButton = new JButton("Search");
    private final JList<Manga> result = new JList<>();

    public SearchForm(Frame parent) {
        super(parent);
        setResizable(false);

        setTitle("Search for manga");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ImageCache.clear();
            }
        });

        var panel = createFieldPanel();

        setContentPane(panel);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }
        });

        searchField.requestFocusInWindow();

        searchButton.addActionListener(e -> search());

        setMinimumSize(new Dimension(600, 1));

        setVisible(true);
        pack();

        setLocationRelativeTo(parent);
    }

    private JPanel createFieldPanel() {
        result.setCellRenderer(new MangaCellRenderer());
        result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        var panel = new JPanel(new MigLayout("fill", "[grow][]", ""));

        var scroll = new JScrollPane(result);
        scroll.setPreferredSize(new Dimension(200, 400));

        panel.add(searchField, "grow");
        panel.add(searchButton, "wrap");
        panel.add(scroll, "span, grow");

        return panel;
    }

    private void search() {
        if (searchField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        searchButton.setEnabled(false);
        SwingUtilities.invokeLater(this::internalSearch);
    }

    private void internalSearch() {
        try {
            var anilist = new AniList();

            var model = new DefaultListModel<Manga>();

            var ret = anilist.search(searchField.getText());
            ret.forEach(model::addElement);

            result.setModel(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage(), "Search", JOptionPane.ERROR_MESSAGE);
        } finally {
            searchButton.setEnabled(true);
        }
    }
}
