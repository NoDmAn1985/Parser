package ru.nodman.parser.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.resources.Resources;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateParser {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);

    private static final String TODAY = "Сегодня";
    private static final String YESTERDAY = "Вчера";

    private DateParser() {
    }

    public static LocalDateTime parse(String dateString, String datePattern) {
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
            String tempFullDate;
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
        try {
            return LocalDateTime.parse(fullDate, dateTimeFormatter);
        } catch (RuntimeException e) {
            LOG.error("Ошибка распознования даты - {}, {}", fullDate, e);
            return null;
        }
    }
}
