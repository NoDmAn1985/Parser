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
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Parser {
    String url;

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class.getSimpleName());


    public Deque<Page> parseUrl(String urlAddress) throws IOException, InterruptedException {
        Deque<Page> pages = new ConcurrentLinkedDeque<>();
        Document docPage;
        try {
            docPage = getDocument(urlAddress, 30000);
        } catch (org.jsoup.UncheckedIOException | IOException ex) {
            LOG.error("не удалось распарсить страницу", ex);
            throw new IOException(ex);
        }

        Elements elementsOnPage = getLinks(docPage);

        LOG.debug("elementsOnPage.size() = {}", elementsOnPage.size());

        for (Element element : elementsOnPage) {
            String address = getAddress(element).replace(Resources.PATTERN_FOR_URL, url);
            String name = getName(element);
            LocalDateTime date = null;
            try {
                date = getDate(address);
            } catch (SocketTimeoutException socketEx) {
                LOG.error("проблема с распознаванием даты (SocketTimeoutException), address = {}\", address");
            } catch (NullPointerException | org.jsoup.UncheckedIOException | IOException e) {
                LOG.error("проблема с распознаванием даты, address = {}", address, e);
                continue;
            }
            Link link = new Link(name, address, date);
            LOG.debug("link = {}", link);
            pages.add(new Page(link));
        }

        if (pages.isEmpty()) {
            throw new IOException();
        }
        return pages;
    }

    static Document getDocument(String urlAddress) throws IOException {
        return getDocument(urlAddress, 2000);
    }

    private static Document getDocument(String urlAddress, int timeOut) throws IOException {
        Connection.Response response = Jsoup.connect(urlAddress)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .timeout(timeOut)
                .followRedirects(false)
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
        } catch (org.jsoup.UncheckedIOException | IOException e) {
            LOG.error("была ошибка, не смог распарсить page = {}, {}", page, e);
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