package ru.nodman.parser.model.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.common.Parameters;
import ru.nodman.parser.model.BaseHandler;
import ru.nodman.parser.model.DateParser;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.time.LocalDateTime;

public class Hdreactor extends Parser {
    private static final String DATE_QUERY = "span.n-date";
    private static final String DATE_PATTERN = "d-M-yyyy, HH:mm";
    private static final String LINK_ATTR = "class";
    private static final String LINK_ATTR_VALUE = "news-head h2";
    private static final String ADDRESS_TAG = "href";
    private static final String NAME_TAG = "h2";
    private static final String USER_QUERY = "span.n-author";
    private static final String IMG_QUERY = "div.sstory img";
    private static final String TORRENT_QUERY = "div.info_d1 > a";
    private static final String QUALITY_QUERY = "div.sstory";
    private static final String DOWN_QUERY = "span.li_download_m";
    private static final String PEERS_COUNT = "span.li_swing_m";
    private static final String SEEDS_COUNT = "span.li_distribute_m";
    private BaseHandler baseHandler;

    public Hdreactor(BaseHandler baseHandler, String url) {
        this.baseHandler = baseHandler;
        this.url = url;
    }

    @Override
    protected Elements getLinks(Document docPage) {
        return docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE);
    }

    @Override
    protected String getName(Element element) {
        return element.child(0).tagName(NAME_TAG).text();
    }

    @Override
    protected String getAddress(Element element) {
        return element.child(0).attr(ADDRESS_TAG);
    }

    @Override
    protected void setDownloadsCount(Page page, Document doc) {
        page.setParameter(Parameters.DOWN_COUNT, doc.select(DOWN_QUERY).get(0).text());
    }

    @Override
    protected void setPeersCount(Page page, Document doc) {
        page.setParameter(Parameters.PEERS_COUNT, doc.select(PEERS_COUNT).get(0).text());
    }

    @Override
    protected void setSeedsCount(Page page, Document doc) {
        page.setParameter(Parameters.SEED_COUNT, doc.select(SEEDS_COUNT).get(0).text());
    }

    @Override
    protected void setAbout(Page page, Document doc) {
        String text = doc.select(QUALITY_QUERY).get(0).toString();
        StringBuilder about = new StringBuilder();
        int beginIndex = text.indexOf("<b>Перевод");
        if (beginIndex == -1) {
            beginIndex = text.indexOf("<b>Тип релиза");
        }
        int endIndex = text.indexOf("<br>", beginIndex);
        if (beginIndex != -1 && endIndex != -1) {
            about.append(text.substring(beginIndex, endIndex)
                    .replaceFirst("<b>", "").replaceFirst("</b>", "").trim());
            about.append(System.lineSeparator());
        }

        int aboutBeginIndex = text.indexOf("<b>О фильме");
        if (aboutBeginIndex == -1) {
            aboutBeginIndex = text.indexOf("<b>Описание");
        }
        int aboutEndIndex = text.indexOf("<br>", aboutBeginIndex);
        if (aboutBeginIndex != -1 && aboutEndIndex != -1) {
            about.append(text.substring(aboutBeginIndex, aboutEndIndex)
                    .replaceFirst("<b>", "").replaceFirst("</b>", "").trim());
        }
        page.setParameter(Parameters.ABOUT_TEXT, about.toString());
    }

    @Override
    protected void setQuality(Page page, Document doc) {
        String text = doc.select(QUALITY_QUERY).get(0).toString();
        int qualityBeginIndex = text.indexOf(Resources.PARSER_QUALITY_BEGIN);
        int plus = qualityBeginIndex + Resources.PARSER_QUALITY_BEGIN.length() + 3;
        int qualityEndIndex = Math.min(text.indexOf("<", plus), plus + Resources.INFO_LENGTH);
        if (qualityBeginIndex > 0 && qualityEndIndex > 0) {
            String newText = text.substring(qualityBeginIndex, qualityEndIndex)
                    .replaceFirst("<b>Качество", "").replaceFirst("</b>", "");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < newText.length(); ++i) {
                char letter = newText.charAt(i);
                if (Character.isLetterOrDigit(letter)) {
                    sb.append(letter);
                }
            }
            page.setParameter(Parameters.QUALITY, sb.toString());
        }
    }

    @Override
    protected void setTorrentMagnetLink(Page page, Document doc) {
        page.setParameter(Parameters.TORRENT_MAGNET_LINK, doc.select(TORRENT_QUERY).
                get(0).attr(Resources.PARSER_LINK_ATTR));
    }

    @Override
    protected void setUserName(Page page, Document doc) {
        page.setParameter(Parameters.USER, doc.select(USER_QUERY).get(0).text());
    }

    @Override
    protected void setSize(Page page, Document doc) {
        String size = doc.select(Resources.PARSER_SIZE_QUERY).get(0).text();
        int sizeBeginIndex = size.indexOf(Resources.PARSER_SIZE_TEXT_BEGIN) + Resources.PARSER_SIZE_TEXT_BEGIN.length();
        int sizeEndIndex = size.lastIndexOf(Resources.PARSER_SIZE_TEXT_END) + 1;
        if (sizeBeginIndex > 0 && sizeEndIndex > 0) {
            page.setParameter(Parameters.SIZE, size.substring(sizeBeginIndex, sizeEndIndex));
        }
    }

    @Override
    protected void setImages(Page page, Document doc) {
        Elements images = doc.select(IMG_QUERY);
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

    @Override
    protected LocalDateTime getDate(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        String dateString = doc.select(DATE_QUERY).get(0).text();
        return DateParser.parse(dateString, DATE_PATTERN);
    }
}
