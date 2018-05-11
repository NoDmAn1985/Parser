package ru.nodman.parser.common;

import ru.nodman.parser.model.Downloader;
import ru.nodman.parser.model.parsers.Parser;
import ru.nodman.parser.resources.Resources;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Page implements Runnable {
    private HashMap<Parameters, String> stringParameters = new HashMap<>();
    private ImageIcon[] images = new ImageIcon[10];
    private Link link;
    private DateTimeFormatter myDateFormat = DateTimeFormatter.ofPattern(Resources.FILE_DATE_PATTERN);
    private Parser parser;

    public Page(Link link) {
        this.link = link;
        for (Parameters parameters : Parameters.values()) {
            stringParameters.put(parameters, "-");
        }
        stringParameters.put(Parameters.TITLE, link.getName());
        stringParameters.put(Parameters.URL, link.getAddress());
        if (link.getDate() != null) {
            stringParameters.put(Parameters.DATE, myDateFormat.format(link.getDate()));
        }
        images[0] = Resources.ERROR_404_BIG;
        for (int i = 1; i < images.length; ++i) {
            images[i] = Resources.ERROR_404;
        }
    }

    public void setParameter(Parameters parameter, String value) {
        stringParameters.put(parameter, value);
    }

    public void setImage(int index, String value) {
        images[index] = imageToIcon(value, (index == 0));
    }

    public int getImageCount() {
        return images.length;
    }

    private ImageIcon imageToIcon(String url, boolean isBigImage) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(Downloader.download(url));
        } catch (IOException ignore) {
            //
        }

        if (image == null) {
//            System.out.println("не сработал: " + url);
            if (isBigImage) {
                return Resources.ERROR_404_BIG;
            } else {
                return Resources.ERROR_404;
            }
        }

        int width = isBigImage ? Resources.IMAGE_BIG_WIDTH : Resources.IMAGE_SMALL_WIDTH;
        int height = isBigImage ? Resources.IMAGE_BIG_HEIGHT : Resources.IMAGE_SMALL_HEIGHT;
        if (isBigImage) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int newHeight = (int) (width * (double) imageHeight / imageWidth);
            height = Math.min(height, newHeight);
        }

        Image newImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImage);
    }

    public String getParameter(Parameters parameter) {
        return stringParameters.get(parameter);
    }

    public ImageIcon getImages(int index) {
        return images[index];
    }

    public Link getLink() {
        return link;
    }

    public String getName() {
        return link.getName();
    }

    public LocalDateTime getDate() {
        return link.getDate();
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    @Override
    public void run() {
        parser.parsePage(this);
    }

    public String getAddress() {
        return link.getAddress();
    }

}
