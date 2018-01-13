package ru.nodman.parser.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.nodman.parser.common.*;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class OldParser {
    private TreeMap<String, Page> sortedPages;
    private Document doc;
    private ControlListener controlListener;
    private BaseHandler baseHandler;
    private String url;
    private int oldPageIndex;
    private LocalDateTime lastDate;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Resources.DATE_PATTERN);
    private int countOfLinksOnPage;
    private int countOfLinksInBase;
    private int countOfPagesToSkip;
    private List<Caption> captions;


    public OldParser() {
        captions = BaseHandler.loadCaptions();
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }

    public void parse(Caption caption, List<Page> pages) throws IOException {
        sortedPages = new TreeMap<>();
        oldPageIndex = 0;
        baseHandler = new BaseHandler(Resources.TIME_ZONE);
        new Thread(baseHandler).start();
        url = caption.getAddress();
        lastDate = caption.getDate();
        baseHandler.loadBase(caption);
        countOfLinksInBase = baseHandler.getBaseSize();

        parsePage(url);

        int index = 0;
        boolean notLast = true;
        while (notLast) {
            if (countOfPagesToSkip <= 0) {
                notLast = parsePage(url + Resources.ADD_TO_FIRST_PAGE +
                        (Resources.SECOND_PAGE + index) + "/");
            } else {
                System.out.println("Пропускаю страницы, осталось - " + countOfPagesToSkip);
                --countOfPagesToSkip;
            }
            ++index;
        }
        pages.addAll(sortedPages.values());
    }

    private boolean parsePage(String urlOfPage) throws IOException {
        Document docPage = Jsoup.connect(urlOfPage).get();
        Elements elementsOnPage = docPage.getElementsByAttributeValue("class", "news-head h2");
        boolean isEnd;
        if (countOfLinksOnPage == 0) {
            countOfLinksOnPage = elementsOnPage.size();
        }

        for (Element element : elementsOnPage) {
            String link = element.child(0).attr("href");
            String name = element.child(0).tagName("h2").text();
            System.out.println("имя = " + name);
            isEnd = getPage(link, name);
            if (!isEnd) {
                return false;
            }
        }
        return true;
    }

    private boolean getPage(String address, String name) throws IOException {
        doc = Jsoup.connect(address).get();

        Element element = doc.select("span.n-date").get(0);
        String text = element.text();
        String fullDate = "";
        if (Character.isLetter(text.charAt(0))) {
            if (text.contains("Сегодня,")) {
                LocalDate date = LocalDate.now();
                fullDate = date.getDayOfMonth() + "-" + date.getMonthValue() +
                        "-" + date.getYear() + text.substring(text.indexOf(", "));
            } else if (text.contains("Вчера,")) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                fullDate = c.get(Calendar.DAY_OF_MONTH) + "-" + (c.get(Calendar.MONTH) + 1) +
                        "-" + c.get(Calendar.YEAR) + text.substring(text.indexOf(", "));
            }
        } else {
            fullDate = text;
        }
        System.out.println(fullDate);
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(fullDate, dateFormat);
        } catch (RuntimeException e) {
            System.out.println("Ошибка распознования даты");
            return true;
        }

        boolean isEnd = (date.compareTo(lastDate) < 0);
        System.out.println(date + (isEnd ? " < " : " > ") + lastDate);

        Link link = new Link(name, address, date);

        boolean isLinkInBase = baseHandler.checkLinkInBase(link);

        if (isEnd) {
            if (countOfLinksInBase > 0 && isLinkInBase) {
                --countOfLinksInBase;
                countOfPagesToSkip = countOfLinksInBase / countOfLinksOnPage;
                countOfLinksInBase = 0;
                return true;
            }
            if (oldPageIndex == Resources.OLD_PAGES_COUNT) {
                return false;
            } else if (!isLinkInBase) {
                ++oldPageIndex;
                System.out.println("загружаем старую страницу № " + oldPageIndex);
            }
        }

        if (isLinkInBase) {
            --countOfLinksInBase;
            return true;
        }

        Page page = new Page(link);
        sortedPages.put(name, page);

        new Thread(new Runnable() {
            @Override
            public void run() {
                getParameters(page);
            }
        }).start();

        controlListener.updateSize(sortedPages.size());

        return true;
    }

    private void getParameters(Page page) {
        page.setParameter(Parameters.USER, doc.select("span.n-author").get(0).text());

        Elements images = doc.select("div.sstory img");
        int count = 0;
        String url;
        for (Element image : images) {
            url = image.attr("src");
            if (!checkImage(url)) {
                continue;
            }
            int finalI = count;
            String finalUrl = url;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    page.setImage(finalI, finalUrl);
                }
            }).start();
            ++count;
            if (count >= page.getImageCount()) {
                break;
            }
        }
        getOtherInformation(page);
    }

    private boolean checkImage(String url) {
        if (url.contains("templates")) {
            return false;
        } else if (url.contains("korsar")) {
            return false;
        } else if (url.contains(".gif") && (url.contains("radikal") || url.contains("fastpic") || url.contains("lostpic"))) {
            return false;
        } else if (url.equals("http://s61.radikal.ru/i174/1306/f3/775b612183f9.png")) {
            return false;
        } else if (url.equals("http://s005.radikal.ru/i209/1302/3c/fc90f19240f5.png")) {
            return false;
        } else if (url.equals("http://i89.fastpic.ru/big/2017/0211/e2/41c93cc9a2c33728a0ac52231f2ae1e2.jpg")) {
            return false;
        } else if (url.equals("http://i74.fastpic.ru/big/2015/1122/29/d02c4db597229f13805c6d3ff7a1bd29.png")) {
            return false;
        } else if (url.equals("http://s019.radikal.ru/i619/1612/72/f4732087e609.png")) {
            return false;
        } else if (url.equals("http://i79.fastpic.ru/big/2016/0524/83/4326a57333a42f3551789d6ca6710a83.png")) {
            return false;
        }
        return true;
    }

    private void getOtherInformation(Page page) {
//TODO обложить всё траями
        String size = doc.select(Resources.PARSER_SIZE_QUERY).get(0).text();
        int sizeBeginIndex = size.indexOf(Resources.PARSER_SIZE_TEXT_BEGIN) + Resources.PARSER_SIZE_TEXT_BEGIN.length();
        int sizeEndIndex = size.lastIndexOf(Resources.PARSER_SIZE_TEXT_END) + 1;
        if (sizeBeginIndex > 0 && sizeEndIndex > 0) {
            page.setParameter(Parameters.SIZE, size.substring(sizeBeginIndex, sizeEndIndex));
        }

//        page.setParameter(Parameters.TORRENT_LINK, Resources.URL +
//                doc.select(Resources.PARSER_TORRENT_LINK_QUERY).get(0).attr(Resources.PARSER_LINK_ATTR));

        page.setParameter(Parameters.TORRENT_MAGNET_LINK, doc.select("div.info_d1 > a").
                get(0).attr(Resources.PARSER_LINK_ATTR));

        String text = doc.select("div.sstory").get(0).toString();

        int qualityBeginIndex = text.indexOf(Resources.PARSER_QUALITY_BEGIN);
        int plus = qualityBeginIndex + Resources.PARSER_QUALITY_BEGIN.length() + 3;
        int qualityEndIndex = Math.min(text.indexOf("<", plus), plus + Resources.INFO_LENGTH);
        if (qualityBeginIndex > 0 && qualityEndIndex > 0) {
            String newText = text.substring(qualityBeginIndex, qualityEndIndex)
                    .replaceFirst("<b>Качество", "").replaceFirst("</b>", "");
//                    .replaceFirst(":","");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < newText.length(); ++i) {
                char letter = newText.charAt(i);
                if (Character.isLetterOrDigit(letter)) {
                    sb.append(letter);
                }
            }
            page.setParameter(Parameters.QUALITY, sb.toString());
        }

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

        page.setParameter(Parameters.SEED_COUNT, doc.select("span.li_distribute_m").get(0).text());

        page.setParameter(Parameters.PEERS_COUNT, doc.select("span.li_swing_m").get(0).text());

        page.setParameter(Parameters.DOWN_COUNT, doc.select("span.li_download_m").get(0).text());
    }

    public void saveLink(Page page, boolean last) {
        baseHandler.appendBase(page.getLink(), last);
    }

    public List<Caption> getCaptions() {
        return captions;
    }
}
