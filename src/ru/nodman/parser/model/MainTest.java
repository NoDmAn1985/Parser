package ru.nodman.parser.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.nodman.parser.resources.Resources;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainTest {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://hdreactor.info/606219-opasnaya-zhenschina-a-dangerous-woman-1993-hdtv-1080i.html").get();
        Element element = doc.select("span.n-date").get(0);
        String text = element.text();
        String fullDate = "";
        if (Character.isLetter(text.charAt(0))) {
            if (text.contains("Сегодня,")) {
                LocalDate date = LocalDate.now();
                fullDate = date.getDayOfMonth() + " " +
                        " " + date.getYear() + text.substring(text.indexOf(", ") + 1);
            } else if (text.contains("Вчера,")) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                fullDate = c.get(Calendar.DAY_OF_MONTH) + " " +
                        " " + c.get(Calendar.YEAR) + text.substring(text.indexOf(", ") + 1);
            }
            System.out.println(fullDate);
        }
    }
}