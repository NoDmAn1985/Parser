package ru.nodman.parser.model.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.common.Link;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Parser {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);


    public ConcurrentLinkedDeque<Page> parseUrl(String url) throws IOException {
        ConcurrentLinkedDeque<Page> pages = new ConcurrentLinkedDeque<>();

        Document docPage = Jsoup
                .connect(url)
                .userAgent("Mozilla")
                .ignoreContentType(true)
                .get();
        Elements elementsOnPage = getLinks(docPage);

        List<Thread> threads = new ArrayList<>();
        for (Element element : elementsOnPage) {
            threads.add(new Thread(() -> {
                String address = getAddress(element);
                String name = getName(element);
                LocalDateTime date = null;
                try {
                    date = getDate(address);
                } catch (IOException e) {
                    return;
                }
                Link link = new Link(name, address, date);
                pages.add(new Page(link));
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            LOG.error("{}", e);
        }
        if (pages.isEmpty()) {
            throw new IOException();
        }
        return pages;
    }

    protected abstract LocalDateTime getDate(String address) throws IOException;

    protected abstract String getName(Element element);

    protected abstract String getAddress(Element element);

    protected abstract Elements getLinks(Document docPage);

    public void parsePage(Page page) {
        Document doc;
        try {
            doc = Jsoup.connect(page.getAddress()).get();
        } catch (IOException e) {
            LOG.error("{}", e);
            return;
        }

        setUserName(page, doc);

        setImages(page, doc);

        setSize(page, doc);

        setTorrentMagnetLink(page, doc);

        setQuality(page, doc);

        setAbout(page, doc);

        setSeedsCount(page, doc);

        setPeersCount(page, doc);

        setDownloadsCount(page, doc);
    }

    protected abstract void setDownloadsCount(Page page, Document doc);

    protected abstract void setPeersCount(Page page, Document doc);

    protected abstract void setSeedsCount(Page page, Document doc);

    protected abstract void setAbout(Page page, Document doc);

    protected abstract void setQuality(Page page, Document doc);

    protected abstract void setTorrentMagnetLink(Page page, Document doc);

    protected abstract void setSize(Page page, Document doc);

    protected abstract void setImages(Page page, Document doc);

    protected abstract void setUserName(Page page, Document doc);
}