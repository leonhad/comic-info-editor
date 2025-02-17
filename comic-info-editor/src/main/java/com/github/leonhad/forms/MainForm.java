package com.github.leonhad.forms;

import com.github.leonhad.components.ImageComponent;
import com.github.leonhad.document.Document;
import com.github.leonhad.filefilters.AllSupportedFilter;
import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.utils.OSUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MainForm extends JFrame {

    private Document document;

    private final ImageComponent imageComponent = new ImageComponent();

    private JMenuItem save;

    private JMenu readMenu;

    public MainForm() throws HeadlessException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Comic Info Editor");
        setSize(800, 600);
        setLocationRelativeTo(null);

        var menuBar = getMenu();

        setJMenuBar(menuBar);

        var principal = new JPanel();
        principal.setLayout(new BorderLayout());

        var imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.add(imageComponent, BorderLayout.CENTER);
        principal.add(imagePanel, BorderLayout.CENTER);

        var optionsPanel = new JPanel();
        optionsPanel.setPreferredSize(new Dimension(200, 1));
        var gridbag = new GridBagLayout();
        var c = new GridBagConstraints();

        optionsPanel.setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = GridBagConstraints.RELATIVE;
        var t = new JLabel("Name");
        gridbag.setConstraints(t, c);
        optionsPanel.add(t);

        c.gridwidth  = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        var text = new JTextField();
        gridbag.setConstraints(text, c);
        optionsPanel.add(text);

        //principal.add(optionsPanel, BorderLayout.EAST);

        setContentPane(principal);
        disableMenus();
    }

    private JMenuBar getMenu() {
        var menuBar = new JMenuBar();
        menuBar.add(getFileMenu());

        readMenu = getReadMenu();
        menuBar.add(readMenu);
        return menuBar;
    }

    private JMenu getReadMenu() {
        var menu = new JMenu("Read");
        menu.setMnemonic(KeyEvent.VK_R);

        var firstPage = new JMenuItem("First Page");
        firstPage.setMnemonic(KeyEvent.VK_F);
        firstPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
        firstPage.addActionListener(e -> this.firstPage());
        menu.add(firstPage);

        var lastPage = new JMenuItem("Last Page");
        lastPage.setMnemonic(KeyEvent.VK_L);
        lastPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
        lastPage.addActionListener(e -> this.lastPage());
        menu.add(lastPage);

        var nextPage = new JMenuItem("Next Page");
        nextPage.setMnemonic(KeyEvent.VK_N);
        nextPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        nextPage.addActionListener(e -> this.nextPage());
        menu.add(nextPage);

        var previousPage = new JMenuItem("Previous Page");
        previousPage.setMnemonic(KeyEvent.VK_P);
        previousPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        previousPage.addActionListener(e -> this.previousPage());
        menu.add(previousPage);

        return menu;
    }

    private JMenu getFileMenu() {
        var menu = new JMenu("File");
        menu.setMnemonic('F');
        menu.setText("File");

        var open = new JMenuItem("Open");
        open.setMnemonic('O');
        open.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        open.addActionListener(e -> open());

        menu.add(open);

        save = new JMenuItem("Save");
        save.setMnemonic('S');
        save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        save.addActionListener(e -> save());
        menu.add(save);

        if (!OSUtils.isOSX()) {
            menu.add(new JSeparator());

            var exit = new JMenuItem("Exit");
            exit.setMnemonic('X');
            exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
            exit.addActionListener(e -> System.exit(0));

            menu.add(exit);
        }

        return menu;
    }

    private void open() {
        var open = new JFileChooser();
        open.setFileSelectionMode(JFileChooser.FILES_ONLY);

        var allSupportedFilter = new AllSupportedFilter();
        open.addChoosableFileFilter(allSupportedFilter);
        open.addChoosableFileFilter(new CbzFilter());
        open.addChoosableFileFilter(new ZipFilter());
        open.setFileFilter(allSupportedFilter);

        open.setDialogTitle("Select a comic file...");

        if (open.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                this.document = new Document(open.getSelectedFile());
                showImage();
                enableMenus();
                this.getContentPane().repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showImage() {
        imageComponent.setImage(document.getCurrentImage());
        float height = this.getHeight();
        float proportion = document.getHeight() / height;

        imageComponent.setPreferredSize(new Dimension((int) (document.getWidth() / proportion), (int) height));
        this.pack();
    }

    private void nextPage() {
        try {
            document.nextPage();
            imageComponent.setImage(document.getCurrentImage());
            this.getContentPane().repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void previousPage() {
        try {
            document.previousPage();
            imageComponent.setImage(document.getCurrentImage());
            this.getContentPane().repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void firstPage() {
        try {
            document.firstPage();
            imageComponent.setImage(document.getCurrentImage());
            this.getContentPane().repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lastPage() {
        try {
            document.lastPage();
            imageComponent.setImage(document.getCurrentImage());
            this.getContentPane().repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void save() {

    }

    private void disableMenus() {
        save.setEnabled(false);

        for (var i = 0; i < readMenu.getItemCount(); i++) {
            readMenu.getItem(i).setEnabled(false);
        }
    }

    private void enableMenus() {
        save.setEnabled(true);

        for (var i = 0; i < readMenu.getItemCount(); i++) {
            readMenu.getItem(i).setEnabled(true);
        }
    }
}
