package ru.nodman.parser.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.common.*;
import ru.nodman.parser.model.MainParser;
import ru.nodman.parser.resources.Resources;

import java.util.ArrayList;
import java.util.List;

import static ru.nodman.parser.common.ParserMode.CONNECTING;

public class Controller implements ControlListener {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);

    private List<Page> pages;
    private MainParser mainParser;
    private int index;

    private ViewListener viewListener;

    public Controller() {
        mainParser = new MainParser();
    }

    @Override
    public Page parse(Caption caption) {
        index = 0;
        this.pages = new ArrayList<>();
        mainParser.setControlListener(this);
        try {
            mainParser.parse(caption, pages);
        } catch (InterruptedException e) {
            LOG.error("{}", e);
        }

        if (pages.isEmpty()) {
            return null;
        }
        Page page = pages.get(index);
        mainParser.saveLink(page, pages.size() - 1 == 0);
        return page;
    }

    @Override
    public Page getNext() {
        if (index == pages.size() - 1) {
            return pages.get(index);
        }
        ++index;
        Page nextPage = pages.get(index);
        mainParser.saveLink(nextPage, index == pages.size() - 1);
        return nextPage;
    }

    @Override
    public Page getPrevious() {
        if (index > 0) {
            --index;
        }
        return pages.get(index);
    }

    @Override
    public void updateSize(int size) {
        viewListener.setSize(index, (size == -1 ? pages.size() : size));
    }

    @Override
    public Caption[] getCaptions() {
        return mainParser.getCaptions().toArray(new Caption[0]);
    }

    @Override
    public void changeParserMode(ParserMode mode) {
        viewListener.changeMode(mode);
    }

    public void setViewListener(ViewListener viewListener) {
        this.viewListener = viewListener;
    }


}
