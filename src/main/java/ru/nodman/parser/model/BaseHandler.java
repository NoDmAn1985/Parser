package ru.nodman.parser.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.resources.Resources;
import ru.nodman.parser.common.Caption;
import ru.nodman.parser.common.Link;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);

    private static final Object LOCK = new Object();
    private static final String CLASS_FOR_NAME_VALUE = "org.sqlite.JDBC";
    private static final String URL = "jdbc:sqlite:parser.sqlite";
    private static final String QUERY_SELECT_ALL_FROM = "select * from %s";
    private static final String QUERY_FOR_DATE_SAVE = "UPDATE main SET date='%s' WHERE base='%s'";
    private static final String TABLE_NAME_MAIN = "main";
    private static final String TABLE_NAME_IMG_EXCEPTIONS = "img_exceptions";
    private static final String MSG_DB_CLOSE = "доступ к базе закрыт";
    private static final String MSG_DB_OPEN = "доступ к базе открыт";
    private static final String COLUMN_LABEL_NAME = "name";
    private static final String COLUMN_LABEL_ADDRESS = "address";
    private static final String COLUMN_LABEL_DATE = "date";
    private static final String COLUMN_LABEL_BASE = "base";
    private static final String COLUMN_LABEL_PARSER_NAME = "parser_name";
    private static final String COLUMN_LABEL_FIRST_PAGE_INDEX = "first_page_index";
    private static final String COLUMN_LABEL_PAGE_PATTERN = "page_pattern";
    private final long timeZone;

    private Statement statement;

    private HashSet<String> imgExceptionsContains = new HashSet<>();
    private HashSet<String> imgExceptionsEquals = new HashSet<>();
    private HashSet<Link> base = new HashSet<>();
    private String baseName;
    private ExecutorService executor;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Resources.FILE_DATE_PATTERN);
    private LocalDateTime tempLastDate;
    private boolean isEnd = false;

    BaseHandler(long timeZone) {
        this.timeZone = timeZone;
    }

    static List<Caption> loadCaptions() {
        List<Caption> captions = new LinkedList<>();

        try {
            Class.forName(CLASS_FOR_NAME_VALUE);
        } catch (ClassNotFoundException e) {
            LOG.error("{}", e);
        }
        try (Connection con = DriverManager.getConnection(URL);
             Statement stmt = con.createStatement();
             ResultSet resultForCaptions = stmt.executeQuery(String.format(QUERY_SELECT_ALL_FROM, TABLE_NAME_MAIN))) {
            LOG.info(MSG_DB_OPEN);

            while (resultForCaptions.next()) {
                Caption caption = new Caption();
                captions.add(caption);
                caption.setName(resultForCaptions.getString(COLUMN_LABEL_NAME));
                caption.setAddress(resultForCaptions.getString(COLUMN_LABEL_ADDRESS));
                caption.setDate(resultForCaptions.getTimestamp(COLUMN_LABEL_DATE).toLocalDateTime());
                caption.setBaseName(resultForCaptions.getString(COLUMN_LABEL_BASE));
                caption.setParserName(resultForCaptions.getString(COLUMN_LABEL_PARSER_NAME));
                caption.setFirstPage(resultForCaptions.getInt(COLUMN_LABEL_FIRST_PAGE_INDEX));
                caption.setPagePattern(resultForCaptions.getString(COLUMN_LABEL_PAGE_PATTERN));
            }
            LOG.debug("созданы заголовки - {} шт.", captions.size());
            LOG.info(MSG_DB_CLOSE);
        } catch (SQLException sqlEx) {
            LOG.error("{}", sqlEx);
        }
        return captions;
    }

    @Override
    public void run() {
        synchronized (LOCK) {
            tempLastDate = LocalDateTime.now().minusHours(timeZone);
            executor = Executors.newSingleThreadExecutor();
            base.clear();
            try {
                Class.forName(CLASS_FOR_NAME_VALUE);
            } catch (ClassNotFoundException e) {
                LOG.error("{}", e);
            }
            try (Connection con = DriverManager.getConnection(URL);
                 Statement stmt = con.createStatement()) {
                LOG.info(MSG_DB_OPEN);
                statement = stmt;
                LOCK.notifyAll();

                while (!isEnd) {
                    LOCK.wait();
                }
            } catch (SQLException | InterruptedException e) {
                LOG.error("{}", e);
            }
            LOG.info(MSG_DB_CLOSE);
        }
    }

    int getBaseSize() {
        return base.size();
    }

    void loadBase(Caption caption) throws InterruptedException {
        synchronized (LOCK) {
            while (statement == null) {
                LOCK.wait();
            }
            baseName = caption.getBaseName();
        }

        loadImgExceptions();

        String query = String.format(QUERY_SELECT_ALL_FROM, baseName);

        try (ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString(COLUMN_LABEL_NAME);
                String address = rs.getString(COLUMN_LABEL_ADDRESS);
                LocalDateTime date = rs.getTimestamp(COLUMN_LABEL_DATE).toLocalDateTime();
                Link link = new Link(name, address, date);
                boolean isAdded = base.add(link);
                if (isAdded && caption.getDate().compareTo(link.getDate()) < 0) {
                    LOG.debug("({}): {}", link.hashCode(), link);
                }
            }
        } catch (SQLException e) {
            LOG.error("{}", e);
        }

        LOG.debug("загрузка базы закончена, в базе {} ссылок", base.size());
    }

    private void loadImgExceptions() {
        String query = String.format(QUERY_SELECT_ALL_FROM, TABLE_NAME_IMG_EXCEPTIONS);
        try (ResultSet resultForImages = statement.executeQuery(query)) {
            while (resultForImages.next()) {
                if (resultForImages.getBoolean("equals")) {
                    imgExceptionsEquals.add(resultForImages.getString(COLUMN_LABEL_ADDRESS));
                } else {
                    imgExceptionsContains.add(resultForImages.getString(COLUMN_LABEL_ADDRESS));
                }
            }
            LOG.debug("создан список исключений (contains) - {} шт.", imgExceptionsContains.size());
            LOG.debug("создан список исключений (equals) - {} шт.", imgExceptionsEquals.size());
        } catch (SQLException sqlEx) {
            LOG.error("{}", sqlEx);
        }
    }

    boolean checkLinkInBase(Link link) {
        if (base.contains(link)) {
            LOG.debug("есть в базе: {}", link);
            return true;
        }
        LOG.debug("ссылка новая: ({}), {}", link.hashCode(), link);
        return false;
    }

    public boolean checkLinkImage(String address) {
        if (imgExceptionsEquals.contains(address)) {
            return true;
        } else {
            for (String string : imgExceptionsContains) {
                if (address.contains(string)) {
                    return true;
                }
            }
        }
        LOG.debug("нет в базе изображений - {}", address);
        return false;
    }

    void appendBase(Link link, boolean isLast) {
        isEnd = isLast;
        if (base.contains(link)) {
            return;
        }

        executor.execute(() -> {
            LOG.debug("сохраняется: {}", link);
            base.add(link);
            saveLink(link);

            if (isLast) {
                LOG.debug("сохраняется время: {}", tempLastDate);
                saveDate(tempLastDate);
                LOG.debug("Закрываю пул потоков");
                synchronized (LOCK) {
                    LOCK.notifyAll();
                }
                executor.shutdown();
            }
        });
    }

    private void saveDate(LocalDateTime date) {
        String query = String.format(QUERY_FOR_DATE_SAVE, dateFormat.format(date), baseName);
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOG.error("{}", e);
        }
    }

    private void saveLink(Link link) {
        String query = String.format("INSERT INTO %s(name, address, date) VALUES ('%s', '%s', '%s')",
                baseName, link.getName(), link.getAddress(), dateFormat.format(link.getDate()));
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOG.error("{}", e);
        }
    }
}
