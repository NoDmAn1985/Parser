package ru.nodman.parser.resources;

import com.sun.xml.internal.ws.api.pipe.Engine;
import javafx.print.Collation;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class Resources {
    public static final String DATE_PATTERN = "d-M-yyyy, HH:mm";
    public static final String FILE_DATE_PATTERN = "yyyy-MM-dd HH:mm:00.0";
    public static final long TIME_ZONE = 4;

    public static final int FRAME_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    public static final int FRAME_WIDTH = (int) (FRAME_HEIGHT * 0.95);
    public static final int IMAGE_SMALL_WIDTH = (int) (FRAME_WIDTH / 4.0);
    public static final int IMAGE_SMALL_HEIGHT = (int) (IMAGE_SMALL_WIDTH / 16.0 * 9);
    public static final int IMAGE_BIG_WIDTH = IMAGE_SMALL_WIDTH * 2;
    public static final int IMAGE_BIG_HEIGHT = IMAGE_SMALL_HEIGHT * 5;

    public static final int TITLE_LENGTH = 125;
    public static final int INFO_LENGTH = 19;
    public static final int ABOUT_LENGTH = 300;

    public static final int OPTIMAL_PAGES_COUNT = 10;
    public static final int OLD_PAGES_COUNT = 10;

    private static final int text_size = (int) (FRAME_WIDTH * 0.016);
    private static final int title_size = (int) (text_size * 1.55);

    public static final Font FONT_TEXT = new Font("Verdana", Font.PLAIN, text_size);
    public static final Font FONT_TITLE = new Font("Verdana", Font.BOLD, title_size);
    public static final Border IMAGE_BORDER = BorderFactory.createRaisedBevelBorder();

    public static final String TITLE = "HDReactor MainParser";
    public static final String URL = "http://hdreactor.info";
    public static final String ADD_TO_FIRST_PAGE = "/page/";
    public static final int SECOND_PAGE = 2;

    public static final String DOWNLOAD_PATH = "C:" + File.separator + "download" + File.separator;
    private static final String resourcesPath = "." + File.separator + "images" + File.separator;
    private static final String arrowLeftPath = "arrowLeft.png";
    private static final String arrowRightPath = "arrowRight.png";
    private static final String error404 = "404.png";
    private static final String error404Big = "404big.png";
    private static final String loading = "load.gif";

    public static final ImageIcon ARROW_LEFT = imageToIcon(arrowLeftPath, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ARROW_RIGHT = imageToIcon(arrowRightPath, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ERROR_404 = imageToIcon(error404, IMAGE_SMALL_WIDTH, IMAGE_SMALL_HEIGHT);
    public static final ImageIcon ERROR_404_BIG = imageToIcon(error404Big, IMAGE_BIG_WIDTH, IMAGE_BIG_HEIGHT);
    public static final ImageIcon LOADING_ICON = gifToIcon(loading);

    public static final int LOADING_WIDTH = gifToIcon(loading).getIconWidth();

    public static final String PARSER_SIZE_TEXT_BEGIN = "Размер: ";
    public static final String PARSER_SIZE_TEXT_END = "b |";
    public static final String PARSER_SIZE_QUERY = "div.bord_a1:contains(Размер:)";
//    public static final String PARSER_TORRENT_LINK_QUERY = "div.title > a";
    public static final String PARSER_LINK_ATTR = "href";
    public static final String PARSER_QUALITY_BEGIN = "<b>Качество";
    public static final int THREAD_COUNT = 2;

    private static ImageIcon imageToIcon(String iconPath, int width, int height) {
        Image newImage = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(Resources.class.getClassLoader().getResourceAsStream(iconPath));
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int newHeight = (int) (width * (double) imageHeight / imageWidth);
            newImage = image.getScaledInstance(width, newHeight, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        };
        return new ImageIcon(newImage);
    }

    private static ImageIcon gifToIcon(String iconPath) {
        return new ImageIcon(Resources.class.getClassLoader().getResource(iconPath));
    }

}
