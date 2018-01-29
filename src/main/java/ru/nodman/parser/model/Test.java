package ru.nodman.parser.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://rutor.info/torrent/1309/ja-znaju-kto-ubil-menja_i-know-who-killed-me-2007-dvdrip").get();
        Elements elements = doc.select("td.header");
        String date = null;
        for (Element element : elements) {
            Elements tempElements = element.getElementsContainingText("Залил");
            if (!tempElements.isEmpty()) {
                date = tempElements.get(0).nextElementSibling().children().last().text();
                System.out.println(date);
                return;
            }
        }
        System.out.println("не нашёл");
//        String newDate = date.substring(0, date.indexOf(" ("));
//        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d-M-yyyy H:mm:ss");
//        LocalDateTime resultDate = LocalDateTime.parse(newDate, dateFormat);
//        System.out.println(resultDate);
    }
}
