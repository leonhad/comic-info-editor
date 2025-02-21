package com.github.leonhad.forms;

import com.github.leonhad.components.ImageComponent;
import com.github.leonhad.document.Document;
import com.github.leonhad.document.PageType;
import com.github.leonhad.filefilters.AllSupportedFilter;
import com.github.leonhad.filefilters.CbzFilter;
import com.github.leonhad.filefilters.ZipFilter;
import com.github.leonhad.utils.OSUtils;
import com.github.leonhad.utils.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Optional;

public class MainForm extends JFrame {

    private Document document;

    private final ImageComponent imageComponent = new ImageComponent();

    private final JMenuItem saveMenu = new JMenuItem("Save");

    private final JMenuItem saveAsMenu = new JMenuItem("Save As...");

    private final JMenu readMenu = getReadMenu();

    private final JMenu editMenu = getEditMenu();

    public MainForm() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Comic Info Editor");
        setSize(800, 600);
        setLocationRelativeTo(null);

        setIconImage(Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/images/icon.png")));

        setJMenuBar(getMenu());

        var principal = new JPanel();
        principal.setLayout(new BorderLayout());

        var imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.add(imageComponent, BorderLayout.CENTER);
        principal.add(imagePanel, BorderLayout.CENTER);
        principal.add(getStatusPanel(), BorderLayout.SOUTH);

        setContentPane(principal);
        disableMenus();
    }

    private static JPanel getStatusPanel() {
        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        var status = new JLabel("Ok.");
        status.setPreferredSize(new Dimension(100, status.getPreferredSize().height));
        var fileStatus = new JLabel("", SwingConstants.CENTER);
        var imageStatus = new JLabel("", SwingConstants.RIGHT);
        imageStatus.setPreferredSize(new Dimension(100, imageStatus.getPreferredSize().height));

        StatusBar.getInstance().setStatusBar(status, fileStatus, imageStatus);
        panel.add(status, BorderLayout.WEST);
        panel.add(fileStatus, BorderLayout.CENTER);
        panel.add(imageStatus, BorderLayout.EAST);
        return panel;
    }

    private JMenuBar getMenu() {
        var menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(editMenu);
        menuBar.add(readMenu);
        return menuBar;
    }

    private JMenu getEditMenu() {
        var menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);

        var editInfo = new JMenuItem("Edit metadata");
        editInfo.setMnemonic(KeyEvent.VK_D);
        editInfo.setAccelerator(KeyStroke.getKeyStroke('I', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editInfo.addActionListener(e -> new InfoForm(this, document.getMetadata()));
        menu.add(editInfo);

        menu.addSeparator();

        var setPageType = new JMenu("Set current image as...");
        setPageType.setMnemonic(KeyEvent.VK_T);
        var none = new JMenuItem("None");
        none.setMnemonic(KeyEvent.VK_N);
        none.setAccelerator(KeyStroke.getKeyStroke("F9"));
        none.addActionListener(e -> {
            try {
                document.setPageType(null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        setPageType.add(none);
        setPageType.addSeparator();

        for (var type : PageType.values()) {
            var item = new JMenuItem(type.getDescription());
            Optional.ofNullable(type.getKeyStroke()).ifPresent(item::setAccelerator);

            item.addActionListener(e -> {
                try {
                    document.setPageType(type);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            setPageType.add(item);
        }

        menu.add(setPageType);

        return menu;
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
        previousPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
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

        saveMenu.setMnemonic('S');
        saveMenu.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveMenu.addActionListener(e -> save());
        menu.add(saveMenu);

        saveAsMenu.setMnemonic('A');
        saveAsMenu.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + InputEvent.SHIFT_DOWN_MASK));
        saveAsMenu.addActionListener(e -> saveAs());
        menu.add(saveAsMenu);

        if (!OSUtils.isOSX()) {
            menu.add(new JSeparator());

            var exit = new JMenuItem("Exit");
            exit.setMnemonic('X');
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
            SwingUtilities.invokeLater(() -> {
                try {
                    this.document = new Document(open.getSelectedFile());
                    showImage();
                    enableMenus();
                    this.getContentPane().repaint();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
                }
            });

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
        try {
            document.save();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAs() {
        var savePanel = new JFileChooser("Save As...");

        var allSupportedFilter = new AllSupportedFilter();
        savePanel.addChoosableFileFilter(allSupportedFilter);
        savePanel.addChoosableFileFilter(new CbzFilter());
        savePanel.addChoosableFileFilter(new ZipFilter());
        savePanel.setFileFilter(allSupportedFilter);

        savePanel.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (savePanel.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var selectedFile = savePanel.getSelectedFile();

            try {
                if (selectedFile.exists()) {
                    var confirm = JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName() + " already exists\nConfirm override?", "Confirm Save As...", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        document.saveAs(selectedFile.toPath());
                    }
                } else {
                    document.saveAs(selectedFile.toPath());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void disableMenus() {
        saveMenu.setEnabled(false);
        saveAsMenu.setEnabled(false);

        for (var i = 0; i < readMenu.getItemCount(); i++) {
            readMenu.getItem(i).setEnabled(false);
        }

        for (var i = 0; i < editMenu.getItemCount(); i++) {
            Optional.ofNullable(editMenu.getItem(i)).ifPresent(item -> item.setEnabled(false));
        }
    }

    private void enableMenus() {
        saveMenu.setEnabled(true);
        saveAsMenu.setEnabled(true);

        for (var i = 0; i < readMenu.getItemCount(); i++) {
            readMenu.getItem(i).setEnabled(true);
        }

        for (var i = 0; i < editMenu.getItemCount(); i++) {
            Optional.ofNullable(editMenu.getItem(i)).ifPresent(item -> item.setEnabled(true));
        }
    }
}
