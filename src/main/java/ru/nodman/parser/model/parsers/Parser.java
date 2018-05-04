package ru.nodman.parser.model.parsers;

import org.jsoup.Connection;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Parser {
    String url;

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class.getSimpleName());


    public Deque<Page> parseUrl(String urlAddress) throws IOException, InterruptedException {
        Deque<Page> pages = new ConcurrentLinkedDeque<>();

//        Document docPage = Jsoup
//                .connect(urlAddress)
//                .ignoreContentType(true)
//                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
//                .referrer("http://www.google.com")
//                .timeout(12000)
//                .get();

        Document docPage = getDocument(urlAddress);

        Elements elementsOnPage = getLinks(docPage);
        LOG.debug("elementsOnPage.size() = {}", elementsOnPage.size());

        List<Thread> threads = new ArrayList<>();
        for (Element element : elementsOnPage) {
            threads.add(new Thread(() -> {
                String address = getAddress(element).replace(Resources.PATTERN_FOR_URL, url);
                String name = getName(element);
                LOG.debug("address = {}, name = {}", address, name);
                LocalDateTime date;
                try {
                    date = getDate(address);
                } catch (IOException e) {
                    LOG.error("{}", e);
                    return;
                }
                Link link = new Link(name, address, date);
                LOG.debug("link = {}", link);
                pages.add(new Page(link));
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        if (pages.isEmpty()) {
            throw new IOException();
        }
        return pages;
    }

    public static Document getDocument(String urlAddress) throws IOException {
        Connection.Response response= Jsoup.connect(urlAddress)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .referrer("http://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute();
        Document docPage = response.charset("windows-1251").parse();
        docPage.charset(Charset.forName("UTF-8"));
        return docPage;
    }

    protected abstract LocalDateTime getDate(String address) throws IOException;

    protected abstract String getName(Element element);

    protected abstract String getAddress(Element element);

    protected abstract Elements getLinks(Document docPage);

    public void parsePage(Page page) {
        Document doc;
        try {
            doc = getDocument(page.getAddress());
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