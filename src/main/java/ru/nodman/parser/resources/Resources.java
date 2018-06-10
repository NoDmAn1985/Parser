package ru.nodman.parser.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Resources {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);

    public static final String FILE_DATE_PATTERN = "yyyy-MM-dd HH:mm:00.0";
    public static final String LOGGER_NAME = "LOG";
    public static final long TIME_ZONE = 4;

    public static final int FRAME_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    public static final int FRAME_WIDTH = (int) (FRAME_HEIGHT * 0.95);
    public static final int ICON_SIZE = 120;
    public static final int IMAGE_SMALL_WIDTH = (int) (FRAME_WIDTH / 4.0);
    public static final int IMAGE_SMALL_HEIGHT = (int) (IMAGE_SMALL_WIDTH / 16.0 * 9);
    public static final int IMAGE_BIG_WIDTH = IMAGE_SMALL_WIDTH * 2;
    public static final int IMAGE_BIG_HEIGHT = IMAGE_SMALL_HEIGHT * 5;

    public static final int TITLE_LENGTH = 125;
    public static final int INFO_LENGTH = 19;

    public static final int INFO_PAGES_COUNT = 5;
    public static final int OLD_PAGES_COUNT = 10;

    private static final int TEXT_SIZE = (int) (FRAME_WIDTH * 0.016);
    private static final int TITLE_SIZE = (int) (TEXT_SIZE * 1.55);

    public static final Font FONT_TEXT = new Font("Segoe Print", Font.PLAIN, TEXT_SIZE);
    public static final Font FONT_TITLE = new Font("Segoe Print", Font.BOLD, TITLE_SIZE);
    public static final Border IMAGE_BORDER = BorderFactory.createRaisedBevelBorder();

    public static final String TITLE = "MyParser";
    public static final String TITLE_CONNECTING = "[CONNECTING] " + TITLE;
    public static final String TITLE_PARSING = "[PARSING] " + TITLE;

    public static final String PATTERN_FOR_URL = "{URL}";

    public static final String DOWNLOAD_PATH = File.separator + "parser" + File.separator + "download" + File.separator;
    private static final String ICO_GREEN = "ico_green.png";
    private static final String ICO_RED = "ico_red.png";
    private static final String ICO_YELLOW = "ico_yellow.png";
    private static final String ARROW_LEFT_PATH = "arrowLeft.png";
    private static final String ARROW_RIGHT_PATH = "arrowRight.png";
    private static final String ERROR_404_PATH = "404.png";
    private static final String ERROR_404_BIG_PATH = "404big.png";
    private static final String LOADING_PATH = "load.gif";

    public static final ImageIcon ICON_GREEN = imageToIcon(ICO_GREEN, ICON_SIZE, ICON_SIZE);
    public static final ImageIcon ICON_RED = imageToIcon(ICO_RED, ICON_SIZE, ICON_SIZE);
    public static final ImageIcon ICON_YELLOW= imageToIcon(ICO_YELLOW, ICON_SIZE, ICON_SIZE);
    public static final ImageIcon ARROW_LEFT = imageToIcon(ARROW_LEFT_PATH, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ARROW_RIGHT = imageToIcon(ARROW_RIGHT_PATH, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ERROR_404 = imageToIcon(ERROR_404_PATH, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ERROR_404_BIG = imageToIcon(ERROR_404_BIG_PATH, IMAGE_BIG_WIDTH, IMAGE_BIG_HEIGHT);
    public static final ImageIcon LOADING_ICON = gifToIcon(LOADING_PATH);

    public static final String PARSER_SIZE_TEXT_BEGIN = "Размер: ";
    public static final String PARSER_SIZE_TEXT_END = "b |";
    public static final String PARSER_SIZE_QUERY = "div.bord_a1:contains(Размер:)";
    public static final String PARSER_LINK_ATTR = "href";
    public static final String PARSER_QUALITY_BEGIN = "<b>Качество";
    public static final int THREAD_COUNT = 2;

    private Resources(){
        // for SonarLint
    }

    private static ImageIcon imageToIcon(String iconPath, int width, int height) {
        Image newImage;
        BufferedImage image;
        try {
            image = ImageIO.read(Resources.class.getClassLoader().getResourceAsStream(iconPath));
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int newHeight = (int) (width * (double) imageHeight / imageWidth);
            if (height > width) {
                LOG.info("пока ничего, высота больше ширины");
            }
            newImage = image.getScaledInstance(width, newHeight, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            LOG.error("{}", e);
            throw new IllegalStateException(e);
        }
        return new ImageIcon(newImage);
    }

    private static ImageIcon gifToIcon(String iconPath) {
        URL url = Resources.class.getClassLoader().getResource(iconPath);
        if (url == null) {
            String errorText = "иконка " + iconPath + "отсутствует";
            LOG.error("{}", errorText);
            throw new IllegalStateException(errorText);
        }
        return new ImageIcon(url);
    }

}
