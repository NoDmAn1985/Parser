package ru.nodman.parser.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateParser {

    public static final String TODAY = "Сегодня";
    public static final String YESTERDAY = "Вчера";

    private DateParser() {
    }

    public static LocalDateTime parse(String dateString, String datePattern) {
//        System.out.println("исходная дата = " + dateString);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
        if (dateString == null) {
            return null;
        }

        String fullDate;
        if (Character.isLetter(dateString.charAt(0))) {
        int yearLastIndex = datePattern.lastIndexOf('y');
        int monthLastIndex = datePattern.lastIndexOf('M');
        int dayLastIndex = datePattern.lastIndexOf('d');
        int indexOfDateEnd = Math.max(Math.max(yearLastIndex, monthLastIndex), dayLastIndex) + 1;
        int yearFirstIndex = datePattern.indexOf('y');
        int monthFirstIndex = datePattern.indexOf('M');
        int dayFirstIndex = datePattern.indexOf('d');
        int indexOfDateStart = Math.min(Math.min(yearFirstIndex, monthFirstIndex), dayFirstIndex);
        String newDatePattern = datePattern.substring(indexOfDateStart, indexOfDateEnd);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(newDatePattern);
            String tempFullDate = null;
            LocalDateTime date;
            int dayNameLength;
            if (dateString.contains(TODAY)) {
                date = LocalDateTime.now();
                dayNameLength = TODAY.length();

            } else if (dateString.contains(YESTERDAY)) {
                date = LocalDateTime.now().minusDays(1);
                dayNameLength = YESTERDAY.length();
            } else {
                return null;
            }

            tempFullDate = dateFormatter.format(date);
            if (indexOfDateStart > 0) {
                fullDate = dateString.substring(0, indexOfDateStart + 1) + tempFullDate;
            } else {
                fullDate = tempFullDate + dateString.substring(dayNameLength, dateString.length());
            }
        } else {
            fullDate = dateString;
        }
//        System.out.println(fullDate);
        try {
            return LocalDateTime.parse(fullDate, dateTimeFormatter);
        } catch (RuntimeException e) {
            System.out.println("Ошибка распознования даты - " + fullDate);
            return null;
        }
    }
}
