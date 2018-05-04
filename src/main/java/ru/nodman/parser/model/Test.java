package ru.nodman.parser.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.time.LocalDateTime;

public class Test {
    private static final String LINK_ATTR = "class";
    private static final String LINK_ATTR_VALUE_FIRST = "gai";
    private static final String LINK_ATTR_VALUE_SECOND = "tum";
    private static final String ADDRESS_TAG = "href";

    public static void main(String[] args) throws IOException {
//        Document docPage = Jsoup
//                .connect("http://rutor.is/browse/1/0/0/0")
//                .userAgent("Mozilla")
//                .ignoreContentType(true)
//                .get();
//        Elements elements = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_FIRST);
//        Elements elementsSecond = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_SECOND);
//
//        for (Element element : elements) {
//            System.out.println(element.getElementsByAttributeValueStarting(ADDRESS_TAG, "/torrent/")
//                .text());
//
//        }



//        Document docPage = Jsoup
//                .connect("http://rutor.info/browse/1/0/0/0")
//                .userAgent("Mozilla")
//                .ignoreContentType(true)
//                .get();
//        Elements elementsOnPage = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_FIRST);
//        Elements elementsSecond = docPage.getElementsByAttributeValue(LINK_ATTR, LINK_ATTR_VALUE_SECOND);
//        elementsOnPage.addAll(elementsSecond);
//
//        for (Element element : elementsOnPage) {
//            String address = getAddress(element).replace(Resources.PATTERN_FOR_URL, "http://rutor.info/torrent/");
//            String name = getName(element);
//            System.out.println("address = " + address);
//            System.out.println("name = " + name);
//        }
//
//

        Document doc = Jsoup.connect("http://rutor.is/torrent/609733/win-10-tweaker-5.4-x86/x64-2018-pc-portable-by-xpuct").get();
        Elements elements = doc.getElementById("details").getElementsByTag("img");
        Elements tempElements = doc.select(".hidebody");
        System.out.println(tempElements.size());
        for (Element tempElement : tempElements) {
            System.out.println(tempElement.children());
        }

        for (Element element : elements) {
            System.out.println(element);
        }
    }

    private static String getAddress(Element element) {
        return "{URL}" + element
                .getElementsByAttributeValueStarting(ADDRESS_TAG, "/torrent/")
                .get(0).attr(ADDRESS_TAG);

    }

    private static String getName(Element element) {
        return element
                .getElementsByAttributeValueStarting(ADDRESS_TAG, "/torrent/")
                .text();
    }

}
