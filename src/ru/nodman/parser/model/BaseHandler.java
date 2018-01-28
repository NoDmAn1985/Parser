package ru.nodman.parser.model;

import ru.nodman.parser.common.Caption;
import ru.nodman.parser.common.Link;
import ru.nodman.parser.resources.Resources;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseHandler implements Runnable {
    private static final Object LOCK = new Object();
    private static final String CLASS_FOR_NAME_VALUE = "org.sqlite.JDBC";
    private static final String URL = "jdbc:sqlite:parser.sqlite";
    private static final String CAPTIONS_QUERY = "select * from main";
    private static final String IMG_EXCEPTIONS_QUERY = "select * from img_exceptions";
    private final long timeZone;

    private Statement statement;

    private HashSet<String> imgExceptionsContains = new HashSet<>();
    private HashSet<String> imgExceptionsEquals = new HashSet<>();
    private HashSet<Link> base = new HashSet<>();
    private Caption caption;
    private String baseName;
    private ExecutorService executor;
    private LocalDateTime currentDate;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Resources.FILE_DATE_PATTERN);
    private LocalDateTime tempLastDate;
    private int baseSize;
    private boolean isEnd = false;

    BaseHandler(long timeZone) {
        this.timeZone = timeZone;
    }

    static List<Caption> loadCaptions() {
        List<Caption> captions = new LinkedList<>();

        try {
            Class.forName(CLASS_FOR_NAME_VALUE);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection con = DriverManager.getConnection(URL);
             Statement stmt = con.createStatement();
             ResultSet resultForCaptions = stmt.executeQuery(CAPTIONS_QUERY)) {

            while (resultForCaptions.next()) {
                Caption caption = new Caption();
                captions.add(caption);
                caption.setName(resultForCaptions.getString("name"));
                caption.setAddress(resultForCaptions.getString("address"));
                caption.setDate(resultForCaptions.getTimestamp("date").toLocalDateTime());
                caption.setBaseName(resultForCaptions.getString("base"));
                caption.setParserName(resultForCaptions.getString("parser_name"));
                caption.setFirstPage(resultForCaptions.getInt("first_page_index"));
                caption.setPagePattern(resultForCaptions.getString("page_pattern"));
            }
            System.out.println("созданы заголовки - " + captions.size() + " шт.");
            System.out.println("доступ к базе закрыт");
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
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
                e.printStackTrace();
            }
            try (Connection con = DriverManager.getConnection(URL);
                 Statement stmt = con.createStatement()) {
                statement = stmt;
                LOCK.notifyAll();

                while (!isEnd) {
                    LOCK.wait();
                }
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("доступ к базе закрыт");
        }
    }

    int getBaseSize() {
        return base.size();
    }

    void loadBase(Caption caption) {
        synchronized (LOCK) {
            while (statement == null) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.caption = caption;
            baseName = caption.getBaseName();
        }

//        loadImgExceptions();

        String query = "select * from " + baseName;

        try (ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String address = rs.getString("address");
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                Link link = new Link(name, address, date);
//                System.out.println(link.hashCode() + " - " + link);
                boolean isAdded = base.add(link);
                if (isAdded && caption.getDate().compareTo(link.getDate()) < 0) {
                    System.out.println("(" + link.hashCode() + ") " + link);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("загрузка базы закончена, в базе " + base.size() + " ссылок");
    }

    private void loadImgExceptions() {
        try (ResultSet resultForImages = statement.executeQuery(IMG_EXCEPTIONS_QUERY)) {
            while (resultForImages.next()) {
                if (resultForImages.getBoolean("equals")) {
                    imgExceptionsEquals.add(resultForImages.getString("address"));
                } else {
                    imgExceptionsContains.add(resultForImages.getString("address"));
                }
            }
            System.out.println("создан список исключений (contains) - " + imgExceptionsContains.size() + " шт.");
            System.out.println("создан список исключений (equals) - " + imgExceptionsEquals.size() + " шт.");
            System.out.println("доступ к базе закрыт");
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    boolean checkLinkInBase(Link link) {
        if (base.contains(link)) {
            System.out.println("есть в базе: " + link);
            return true;
        }
//        System.out.println("ссылка новая: " + link);
        System.out.println("ссылка новая: (" + link.hashCode() + ") " + link);
        return false;
    }

    public boolean checkLinkImage(String address) {
        if (imgExceptionsEquals.contains(address)) {
//            System.out.println("есть в базе (equals): " + address);
            return true;
        } else {
            for (String string : imgExceptionsContains) {
                if (address.contains(string)) {
//                    System.out.println("есть в базе (contains): " + address);
                    return true;
                }
            }
        }
//        System.out.println("нет в базе изображений - " + address);
        return false;
    }

    void appendBase(Link link, boolean isLast) {
        isEnd = isLast;
        if (base.contains(link)) {
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("сохраняется: " + link);
                base.add(link);
                saveLink(link);

                if (isLast) {
                    System.out.println("сохраняется время: " + tempLastDate);
                    saveDate(tempLastDate);
                    System.out.println("Закрываю пул потоков");
                    synchronized (LOCK) {
                        LOCK.notifyAll();
                    }
                    executor.shutdown();
                }
            }
        });
    }

    private void saveDate(LocalDateTime date) {
        String query = "UPDATE main SET date='" + dateFormat.format(date) + "' WHERE base='" + baseName + "'";
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveLink(Link link) {
        String query = String.format("INSERT INTO %s(name, address, date) VALUES ('%s', '%s', '%s')",
                baseName, link.getName(), link.getAddress(), dateFormat.format(link.getDate()));
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LocalDateTime getDate() {
        return currentDate;
    }
}
