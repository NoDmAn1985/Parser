package ru.nodman.parser.model.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.common.Parameters;
import ru.nodman.parser.model.BaseHandler;
import ru.nodman.parser.model.DateParser;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.time.LocalDateTime;

public class Rutor extends Parser {
    private static final String DATE_QUERY = "td.header";
    private static final String DATE_PATTERN = "d-M-yyyy H:mm:ss";
    private static final String LINK_ATTR = "class";
    private static final String LINK_ATTR_VALUE_FIRST = "gai";
    private static final String LINK_ATTR_VALUE_SECOND = "tum";
    private static final String ADDRESS_TAG = "href";
    private static final String NAME_TAG = "h2";
    private static final String IMG_QUERY = "div.sstory img";
    private static final String TORRENT_QUERY = "#download";
    private static final String QUALITY_QUERY = "div.sstory";
    private static final String DOWN_QUERY = "span.li_download_m";
    private static final String PEERS_COUNT = "Качают";
    private static final String SEEDS_COUNT = "Раздают";
    private static final String USER_QUERY = "span.n-author";
    private static final String ABOUT_QUERY = ".hidewrap";
    private static final String PARSER_LINK_ATTR = "href";
    private static final String SIZE_QUERY = "Размер";
    private BaseHandler baseHandler;

    public Rutor(BaseHandler baseHandler, String url) {
        this.baseHandler = baseHandler;
        this.url = url;
    }

    @Override
    protected Elements getLinks(Document docPage) {
        Elements elements = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_FIRST);
        Elements elementsSecond = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_SECOND);
        elements.addAll(elementsSecond);
        return elements;
    }

    @Override
    protected String getAddress(Element element) {
        return "{URL}" + element
                .getElementsByAttributeValueStarting(ADDRESS_TAG, "/torrent/")
                .get(0).attr(ADDRESS_TAG);
    }

    @Override
    protected String getName(Element element) {
        return element
                .getElementsByAttributeValueStarting(ADDRESS_TAG, "/torrent/")
                .text();
    }

    @Override
    protected LocalDateTime getDate(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        String date = getParameter(doc, "Добавлен");
        if (date == null) {
            return null;
        }
        String newDate = date.substring(0, date.indexOf(" ("));
        return DateParser.parse(newDate, DATE_PATTERN);
    }

    @Override
    protected void setDownloadsCount(Page page, Document doc) {
        page.setParameter(Parameters.DOWN_COUNT, "-");
    }

    @Override
    protected void setPeersCount(Page page, Document doc) {
        page.setParameter(Parameters.PEERS_COUNT, getParameter(doc, PEERS_COUNT));
    }

    @Override
    protected void setSeedsCount(Page page, Document doc) {
        page.setParameter(Parameters.SEED_COUNT, getParameter(doc, SEEDS_COUNT));
    }

    @Override
    protected void setAbout(Page page, Document doc) {
        String text = "<html>" + doc.select(ABOUT_QUERY).first().text() + "</html>";
        page.setParameter(Parameters.ABOUT_TEXT, text);
    }

    @Override
    protected void setQuality(Page page, Document doc) {
        page.setParameter(Parameters.QUALITY, "-");
    }

    @Override
    protected void setTorrentMagnetLink(Page page, Document doc) {
        page.setParameter(Parameters.TORRENT_MAGNET_LINK, doc.select(TORRENT_QUERY).
                first().attr(PARSER_LINK_ATTR));
    }

    @Override
    protected void setUserName(Page page, Document doc) {
        page.setParameter(Parameters.USER,  "-");
    }

    @Override
    protected void setSize(Page page, Document doc) {
        String text = getParameter(doc, SIZE_QUERY);
        String newText = (text == null ? "-" : text.replaceAll("\\(.*\\)", ""));
        page.setParameter(Parameters.USER,  newText);
    }

    @Override
    protected void setImages(Page page, Document doc) {
        Elements images = doc.select("#details").first().getElementsByAttributeValueStarting("src", "http://").select("img");
        int count = 0;
        String url;
        for (Element image : images) {
            url = image.attr("src");
            if (baseHandler.checkLinkImage(url)) {
                continue;
            }
            int finalI = count;
            String finalUrl = url;
            new Thread(() -> page.setImage(finalI, finalUrl)).start();
            ++count;
            if (count >= page.getImageCount()) {
                break;
            }
        }
    }

    private String getParameter(Document doc, String textOfElement) {
        Elements elements = doc.select("td.header");
        for (Element element : elements) {
            Elements tempElements = element.getElementsContainingText(textOfElement);
            if (!tempElements.isEmpty()) {
                return tempElements.get(0).nextElementSibling().text();
            }
        }
        return null;
    }
}
