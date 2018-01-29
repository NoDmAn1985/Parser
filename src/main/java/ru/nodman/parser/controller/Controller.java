package ru.nodman.parser.controller;

import ru.nodman.parser.common.Caption;
import ru.nodman.parser.common.ControlListener;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.common.ViewListener;
import ru.nodman.parser.model.MainParser;

import java.util.ArrayList;
import java.util.List;

public class Controller implements ControlListener {
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
        mainParser.parse(caption, pages);

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

    public void setViewListener(ViewListener viewListener) {
        this.viewListener = viewListener;
    }

}
