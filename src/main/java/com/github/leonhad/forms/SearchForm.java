package com.github.leonhad.forms;

import com.github.leonhad.components.MangaCellRenderer;
import com.github.leonhad.integration.ImageCache;
import com.github.leonhad.integration.Manga;
import com.github.leonhad.integration.anilist.AniList;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

public class SearchForm extends JDialog {

    public static final String SEARCH_TEXT = "Search";

    private final JTextField searchField = new JTextField("");
    private final JButton searchButton = new JButton(SEARCH_TEXT);
    private final JButton selectButton = new JButton("Select");
    private final JList<Manga> result = new JList<>();
    private final JProgressBar progressBar = new JProgressBar();

    @Getter
    private Manga selectedItem;

    public static void main(String[] args) {
        new SearchForm(null);
    }

    public SearchForm(Frame parent) {
        super(parent, true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setTitle("Search for manga");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ImageCache.clear();
            }
        });

        var panel = createFieldPanel();

        setContentPane(panel);

        selectedItem = null;

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        searchField.requestFocusInWindow();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN && result.getModel().getSize() > 0) {
                    result.requestFocus();
                    result.setSelectedIndex(0);
                }
            }
        });

        searchButton.addActionListener(e -> search());
        selectButton.setMnemonic('s');

        selectButton.setEnabled(false);
        selectButton.setMnemonic('t');
        selectButton.addActionListener(e -> select());

        result.addListSelectionListener(e -> selectButton.setEnabled(!result.isSelectionEmpty()));
        result.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && searchButton.isEnabled()) {
                    select();
                }
            }
        });

        result.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        setMinimumSize(new Dimension(600, 1));

        pack();

        setLocationRelativeTo(parent);

        setVisible(true);
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
        panel.add(progressBar, "span, grow");

        panel.add(new JPanel());
        panel.add(selectButton, "align trailing");

        return panel;
    }

    private void select() {
        this.selectedItem = result.getSelectedValue();
        this.setVisible(false);
    }

    private void search() {
        if (searchField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        searchButton.setText("Searching...");
        progressBar.setVisible(true);
        searchButton.setEnabled(false);

        new SwingWorker<DefaultListModel<Manga>, Void>() {

            @Override
            protected DefaultListModel<Manga> doInBackground() throws Exception {
                var anilist = new AniList();
                var ret = anilist.search(searchField.getText());
                var model = new DefaultListModel<Manga>();
                ret.forEach(model::addElement);
                return model;
            }

            @Override
            protected void done() {
                try {
                    result.setModel(get());
                    result.clearSelection();
                    selectButton.setEnabled(false);
                } catch (InterruptedException | ExecutionException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(SearchForm.this, "Search failed: " + ex.getMessage(), SEARCH_TEXT, JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SearchForm.this, "Search failed: " + ex.getMessage(), SEARCH_TEXT, JOptionPane.ERROR_MESSAGE);
                } finally {
                    searchButton.setText(SEARCH_TEXT);
                    progressBar.setVisible(false);
                    searchButton.setEnabled(true);
                }
            }
        }.execute();
    }
}
