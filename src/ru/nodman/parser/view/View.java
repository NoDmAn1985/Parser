package ru.nodman.parser.view;

import ru.nodman.parser.common.*;
import ru.nodman.parser.resources.Resources;
import uriSchemeHandler.CouldNotOpenUriSchemeHandler;
import uriSchemeHandler.URISchemeHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class View implements ViewListener {
    private ControlListener controlListener;
    private int size;
    private Page page;
    private Date dateLoad;
    private boolean isLoadEnds = false;

    private JFrame frame = new JFrame();

    private JLabel number;
    private JLabel title;
    private JLabel creationDate;
    private JLabel user;
    private JLabel quality;
    private JLabel fileSize;

    private ArrayList<JLabel> img = new ArrayList<>();

    private JButton parse;
    private JButton left;
    private JButton right;
    private JButton link;
    private JButton btnTorrentMagnetLink;
    private ArrayList<JButton> buttons;

    private JLabel seeds;
    private JLabel peers;
    private JLabel downs;
    private JTextArea aboutText;
    private JComboBox<Caption> url;

    private String magnetLink;
    private String torrentLink;
    private String urlLink;

    private GridBagConstraints constants = new GridBagConstraints();


    public void startApplication() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            initFrame();
            initContent();


            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        int code = e.getKeyCode();
                        switch (code) {
                            case 37:
                                getNextPage(false);
                                break;
                            case 39:
                                getNextPage(true);
                                break;
                        }
                    }
                    return false;
                }
            });

        });
    }

    private void initFrame() {
        frame = new JFrame();
        frame.setSize(Resources.FRAME_WIDTH, Resources.FRAME_HEIGHT);
        frame.setTitle(Resources.TITLE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.setIconImage(this.iconManager.getIcon());
        frame.setVisible(true);
    }

    private void initContent() {
        for (int i = 0; i < 10; ++i) {
            img.add(new JLabel());
        }

        frame.setLayout(new GridBagLayout());
        constants.weightx = 1;
        constants.weighty = 1;
        constants.fill = GridBagConstraints.BOTH;

        constants.gridy = 0;
        constants.gridx = 0;
        constants.gridwidth = 3;
        constants.gridheight = 1;
        url = new JComboBox<>(controlListener.getCaptions());
        url.setFont(Resources.FONT_TITLE);
        ((JLabel) url.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        url.setSelectedIndex(0);
        frame.add(url, constants);

        constants.gridy = 0;
        constants.gridx = 3;
        constants.gridwidth = 1;
        constants.gridheight = 1;
        parse = new JButton("Поиск");
        parse.setFont(Resources.FONT_TITLE);
        parse.setMargin(new Insets(5, 0, 5, 0));
        parse.addActionListener(e -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    parse();
                }
            }).start();
        });
        frame.add(parse, constants);

        constants.gridy = 1;
        constants.gridx = 0;
        constants.gridwidth = 1;
        constants.gridheight = 1;
        left = new JButton();
        left.setMargin(new Insets(0, 0, 0, 0));
        left.setIcon(Resources.ARROW_LEFT);
        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNextPage(false);
            }
        });
        frame.add(left, constants);

        addImg(img.get(0), 1, 1, 2, 5, false);

        img.get(0).setLayout(new GridBagLayout());

        GridBagConstraints titleConstants = new GridBagConstraints();
        titleConstants.weightx = 1;
        titleConstants.gridx = 0;
        titleConstants.fill = GridBagConstraints.BOTH;

        number = new JLabel();
        number.setFont(Resources.FONT_TEXT);
        number.setHorizontalAlignment(JLabel.CENTER);
        number.setVerticalAlignment(JLabel.BOTTOM);
        number.setForeground(Color.WHITE);
        setSize(0, 0);
        titleConstants.gridy = 0;
        titleConstants.weighty = 0.01;
        img.get(0).add(number, titleConstants);

        title = new JLabel();
        title.setFont(Resources.FONT_TITLE);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.TOP);
        title.setForeground(Color.WHITE);
        setTitle("Заголовок");
        titleConstants.gridy = 1;
        titleConstants.weighty = 1;
        img.get(0).add(title, titleConstants);

        constants.gridy = 1;
        constants.gridx = 3;
        constants.gridwidth = 2;
        constants.gridheight = 1;
        right = new JButton();
        right.setMargin(new Insets(0, 0, 0, 0));
        right.setIcon(Resources.ARROW_RIGHT);
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNextPage(true);
            }
        });
        frame.add(right, constants);

        constants.gridy = 2;
        constants.gridx = 0;
        constants.gridwidth = 1;
        constants.gridheight = 1;
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBorder(Resources.IMAGE_BORDER);
        frame.add(infoPanel, constants);

        GridBagConstraints infoConstants = new GridBagConstraints();
        infoConstants.weightx = 1;
        infoConstants.weighty = 1;
        infoConstants.gridx = 0;
        infoConstants.fill = GridBagConstraints.BOTH;

        infoConstants.gridy = 0;
        creationDate = new JLabel();
        creationDate.setFont(Resources.FONT_TEXT);
        creationDate.setHorizontalAlignment(JLabel.CENTER);
        creationDate.setText("Дата");
        creationDate.setBorder(Resources.IMAGE_BORDER);
        infoPanel.add(creationDate, infoConstants);

        infoConstants.gridy = 1;
        user = new JLabel();
        user.setFont(Resources.FONT_TEXT);
        user.setHorizontalAlignment(JLabel.CENTER);
        user.setForeground(Color.BLUE);
        user.setText("Создатель");
        user.setBorder(Resources.IMAGE_BORDER);
        infoPanel.add(user, infoConstants);

        infoConstants.gridy = 2;
        quality = new JLabel();
        quality.setFont(Resources.FONT_TITLE);
        quality.setHorizontalAlignment(JLabel.CENTER);
        quality.setText("Качество");
        quality.setBorder(Resources.IMAGE_BORDER);
        infoPanel.add(quality, infoConstants);

        infoConstants.gridy = 3;
        fileSize = new JLabel();
        fileSize.setFont(Resources.FONT_TITLE);
        fileSize.setHorizontalAlignment(JLabel.CENTER);
        fileSize.setForeground(Color.RED);
        fileSize.setText("Размер");
        fileSize.setBorder(Resources.IMAGE_BORDER);
        infoPanel.add(fileSize, infoConstants);

        addImg(img.get(1), 2, 3, 1, 1, true);
        addImg(img.get(2), 3, 0, 1, 1, true);
        addImg(img.get(3), 3, 3, 1, 1, true);
        addImg(img.get(4), 4, 0, 1, 1, true);
        addImg(img.get(5), 4, 3, 1, 1, true);
        addImg(img.get(6), 5, 0, 1, 1, true);
        addImg(img.get(7), 5, 3, 1, 1, true);
        addImg(img.get(8), 6, 0, 1, 1, true);
        addImg(img.get(9), 6, 3, 1, 1, true);

        constants.gridy = 6;
        constants.gridx = 1;
        constants.gridwidth = 2;
        constants.gridheight = 1;
        JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new GridBagLayout());
        linkPanel.setBorder(Resources.IMAGE_BORDER);
        frame.add(linkPanel, constants);

        GridBagConstraints linkConstants = new GridBagConstraints();
        linkConstants.weightx = 1;
        linkConstants.weighty = 1;
        linkConstants.gridx = 0;
        linkConstants.fill = GridBagConstraints.BOTH;

        linkConstants.gridy = 0;
        link = new JButton();
        link.setFont(Resources.FONT_TEXT);
        link.setText("открыть в браузере");
        link.setMargin(new Insets(5, 0, 5, 0));
        link.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    System.out.println(urlLink);
                    Desktop.getDesktop().browse(new URI(urlLink));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        linkPanel.add(link, linkConstants);

        linkConstants.gridy = 1;
        btnTorrentMagnetLink = new JButton("торрент-магнит");
        btnTorrentMagnetLink.setFont(Resources.FONT_TITLE);
        btnTorrentMagnetLink.setMargin(new Insets(5, 0, 5, 0));
        btnTorrentMagnetLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URI magnetLinkUri;
                try {
                    magnetLinkUri = new URI(magnetLink);
                    URISchemeHandler uriSchemeHandler = new URISchemeHandler();
                    uriSchemeHandler.open(magnetLinkUri);
                } catch (URISyntaxException | CouldNotOpenUriSchemeHandler e1) {
                    e1.printStackTrace();
                }
            }
        });
        linkPanel.add(btnTorrentMagnetLink, linkConstants);

        linkConstants.gridy = 2;
        JPanel downPanel = new JPanel();
        downPanel.setLayout(new GridBagLayout());
        downPanel.setBorder(Resources.IMAGE_BORDER);
        linkPanel.add(downPanel, linkConstants);

        GridBagConstraints downConstants = new GridBagConstraints();
        downConstants.weightx = 1;
        downConstants.gridy = 0;
        downConstants.fill = GridBagConstraints.BOTH;

        downConstants.gridx = 0;
        seeds = new JLabel();
        seeds.setFont(Resources.FONT_TEXT);
        seeds.setHorizontalAlignment(JLabel.CENTER);
        seeds.setText("Раздают: ?");
        downPanel.add(seeds, downConstants);

        downConstants.gridx = 1;
        peers = new JLabel();
        peers.setFont(Resources.FONT_TEXT);
        peers.setHorizontalAlignment(JLabel.CENTER);
        peers.setText("Качают: ?");
        downPanel.add(peers, downConstants);

        downConstants.gridx = 2;
        downConstants.weightx = 2;
        downs = new JLabel();
        downs.setFont(Resources.FONT_TEXT);
        downs.setHorizontalAlignment(JLabel.CENTER);
        downs.setText("Скачали: ?");
        downPanel.add(downs, downConstants);

        constants.gridy = 7;
        constants.gridx = 0;
        constants.gridwidth = 4;
        constants.gridheight = 1;
        aboutText = new JTextArea();
        aboutText.setFont(Resources.FONT_TEXT);
        aboutText.setEditable(false);
        aboutText.setLineWrap(true);
        aboutText.setMinimumSize(new Dimension(Resources.FRAME_WIDTH, Resources.IMAGE_SMALL_HEIGHT));
        aboutText.setText("О фильме:");
        frame.add(aboutText, constants);

        buttons = new ArrayList<>(Arrays.asList(left, right, link, btnTorrentMagnetLink));
        setButtonsOn(false);
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }

    @Override
    public void showErrorMsg() {

    }

    private void addImg(JLabel jlabel, int gridY, int gridX, int gridWidth, int gridHeight, boolean isSmall) {
        constants.gridy = gridY;
        constants.gridx = gridX;
        constants.gridwidth = gridWidth;
        constants.gridheight = gridHeight;
        if (isSmall) {
            jlabel.setIcon(Resources.ERROR_404);
        } else {
            jlabel.setIcon(Resources.ERROR_404_BIG);
        }
        jlabel.setBorder(Resources.IMAGE_BORDER);
        frame.add(jlabel, constants);
    }

    private void showPage() {
        setTitle(page.getParameter(Parameters.TITLE));
        creationDate.setText(page.getParameter(Parameters.DATE));
        for (int i = 0; i < page.getImageCount(); ++i) {
            img.get(i).setIcon(page.getImages(i));
        }

        user.setText(page.getParameter(Parameters.USER));
        quality.setText(page.getParameter(Parameters.QUALITY));
        fileSize.setText(page.getParameter(Parameters.SIZE));
        urlLink = page.getParameter(Parameters.URL);
//        torrentLink = page.getParameter(Parameters.TORRENT_LINK);
        magnetLink = page.getParameter(Parameters.TORRENT_MAGNET_LINK);
        seeds.setText("Раздают: " + page.getParameter(Parameters.SEED_COUNT));
        peers.setText("Качают: " + page.getParameter(Parameters.PEERS_COUNT));
        downs.setText("Скачали: " + page.getParameter(Parameters.DOWN_COUNT));
        aboutText.setText(page.getParameter(Parameters.ABOUT_TEXT));
        controlListener.updateSize(-1);
    }

    @Override
    public void setSize(int pageNumber, int size) {
        this.size = size;
        number.setText("<html><p align = 'center' bgcolor = 'blue'>&lt; " +
                (page == null ? 0 : (pageNumber + 1)) + " из " + size + " &gt;" +
                "</p></html>");
    }

    private void setTitle(String text) {
        title.setText("<html><p align = 'center' ><span bgcolor = 'blue'>" +
                text.substring(0, Math.min(text.length(), Resources.TITLE_LENGTH)) +
                "</span></p></html>");
    }

    private void setButtonsOn(boolean isButtonOn) {
        for (JButton button : buttons) {
            button.setEnabled(isButtonOn);
        }
    }

    private void getNextPage(boolean isRight) {
        if (!isLoadEnds) {
            return;
        }
        if (isRight) {
            page = controlListener.getNext();
        } else {
            page = controlListener.getPrevious();
        }
        showPage();
    }


    private void parse() {
        Resources.LOADING_ICON.setImageObserver(img.get(0));
        parse.setEnabled(false);
        setButtonsOn(false);
        isLoadEnds = false;
        img.get(0).setIcon(Resources.LOADING_ICON);
        img.get(0).setHorizontalAlignment(JLabel.CENTER);
        img.get(0).setMinimumSize(new Dimension(Resources.IMAGE_BIG_WIDTH, Resources.IMAGE_BIG_HEIGHT));
        page = controlListener.parse((Caption) url.getSelectedItem());
        showPage();
        parse.setEnabled(true);
        setButtonsOn(true);
        isLoadEnds = true;
    }

}
