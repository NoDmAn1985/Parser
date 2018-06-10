package ru.nodman.parser.common;

import ru.nodman.parser.resources.Resources;

import java.awt.*;


public enum ParserMode {
    MAIN(Resources.ICON_GREEN.getImage(), Resources.TITLE),
    CONNECTING(Resources.ICON_YELLOW.getImage(), Resources.TITLE_CONNECTING),
    PARSING(Resources.ICON_RED.getImage(), Resources.TITLE_PARSING);

    private String title;
    private Image icon;

    ParserMode(Image icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Image getIcon() {
        return icon;
    }
}
