package ru.nodman.parser.model.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.nodman.parser.common.Link;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.resources.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Parser {

    public ConcurrentLinkedDeque<Page> parseUrl(String url) throws IOException {
        ConcurrentLinkedDeque<Page> pages = new ConcurrentLinkedDeque<>();

//        System.setProperty("https.proxyHost", Resources.PROXY_ADDRESS);
//        System.setProperty("https.proxyPort", String.valueOf(Resources.PROXY_PORT));

        Document docPage = Jsoup
//                .connect(getUrlWithProxy(url))
                .connect(url)
//                .proxy(Resources.PROXY)
//                .proxy(Resources.PROXY_ADDRESS, Resources.PROXY_PORT)
                .userAgent("Mozilla")
//                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
//                .header("Content-Language", "en-US")
                .ignoreContentType(true)
//                .followRedirects(false)
//                .timeout(5000)
                .get();
        Elements elementsOnPage = getLinks(docPage);
        System.out.println("найдено " + elementsOnPage.size() + " элементов");

        List<Thread> threads = new ArrayList<>();
        for (Element element : elementsOnPage) {
            threads.add(new Thread(() -> {
                System.out.println(1);
                String address = getAddress(element);
                String name = getName(element);
                LocalDateTime date = null;
                try {
                    System.out.println(2);
                    date = getDate(address);
                } catch (IOException e) {
                    System.out.println(-3);
                    return;
                }
                    System.out.println(3);
                Link link = new Link(name, address, date);
                pages.add(new Page(link));
                System.out.println("Link = " + link);
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
                System.out.println("дождался " + thread.getName());
            }
            System.out.println("дождался всех = " + threads.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (pages.isEmpty()) {
            throw new IOException();
        }
        System.out.println("pages.size() = " + pages.size());
        return pages;
    }

//    private String getUrlWithProxy(String address) throws IOException {
//        URL url = new URL(address);
//        HttpURLConnection uc = (HttpURLConnection) url.openConnection(Resources.PROXY);
//        uc.connect();
//        String line;
//        StringBuilder tmp = new StringBuilder();
//        try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()))) {
//            while ((line = in.readLine()) != null) {
//                tmp.append(line);
//            }
//        }
//        return String.valueOf(tmp);
//    }

    protected abstract LocalDateTime getDate(String address) throws IOException;

    protected abstract String getName(Element element);

    protected abstract String getAddress(Element element);

    protected abstract Elements getLinks(Document docPage);

    public void parsePage(Page page) {
        Document doc;
        try {
            doc = Jsoup
                    .connect(page.getAddress())
//                    .proxy(Resources.PROXY)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
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