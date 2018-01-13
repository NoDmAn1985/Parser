package ru.nodman.parser.model;

import ru.nodman.parser.common.*;
import ru.nodman.parser.model.parsers.Parser;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainParser {
    private static final long SLEEPING_TIME = 5000;
    private TreeMap<String, Page> sortedPages;
    private ControlListener controlListener;
    private BaseHandler baseHandler;
    private String url;
    private int oldPageCountToLoad;
    private LocalDateTime lastDate;
    private int countOfLinksOnPage;
    private int countOfLinksInBase;
    private int countOfPagesToSkip;
    private List<Caption> captions;
    private ExecutorService executor;
    private Parser parser;

    public MainParser() {
        captions = BaseHandler.loadCaptions();
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }

    public void parse(Caption caption, List<Page> pages) {
        baseHandler = new BaseHandler(Resources.TIME_ZONE);
        new Thread(baseHandler).start();

        oldPageCountToLoad = Resources.OLD_PAGES_COUNT;

        baseHandler.loadBase(caption);
        countOfLinksInBase = baseHandler.getBaseSize();

        executor = Executors.newFixedThreadPool(Resources.THREAD_COUNT);

        sortedPages = new TreeMap<>();
        url = caption.getAddress();
        lastDate = caption.getDate();
        System.out.println("последняя дата - " + lastDate);
        int index = caption.getFirstPage();
        String pagePattern = caption.getPagePattern();

        try {
            parser = setParser(caption.getParserName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            e.printStackTrace();
        }

        boolean isEnd = false;
        while (!isEnd) {
            System.out.println("oldPageCountToLoad = " + oldPageCountToLoad);
            ConcurrentLinkedDeque<Page> tempListOfPage = null;
            try {
                String address = url + String.format(pagePattern, index);
                System.out.println("смотрю страницу - " + address);
                tempListOfPage = parser.parseUrl(address);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (countOfLinksOnPage == 0) {
                countOfLinksOnPage = tempListOfPage.size();
            }

            boolean isWrongPages = !addPages(tempListOfPage);
            if (isWrongPages) {
                if (countOfPagesToSkip > 0) {
                    System.out.println("Пропускаю страницы, осталось - " + countOfPagesToSkip);
                    index += countOfPagesToSkip;
                    countOfPagesToSkip = 0;
                } else if (oldPageCountToLoad < 0) {
                    System.out.println("найден предел");
                    isEnd = true;
                }
            }
            ++index;
        }
        pages.addAll(sortedPages.values());

        try {
            Thread.sleep(SLEEPING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    private Parser setParser(String parserName) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String className = "ru.nodman.parser.model.parsers." + parserName;
        Class<?> cls = Class.forName(className);
        if (!Parser.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException();
        }
        return (Parser) cls.getConstructor(BaseHandler.class).newInstance(baseHandler);
    }

    private boolean addPages(ConcurrentLinkedDeque<Page> tempListOfPage) {
        System.out.println(tempListOfPage.size());
        tempListOfPage.forEach(System.out::println);

        for (Page page : tempListOfPage) {
            System.out.println("имя = " + page.getName());
            boolean isOldLink = !checkDate(page.getDate());

            if (!baseHandler.checkLinkInBase(page.getLink())) {
                if (isOldLink) {
                    --oldPageCountToLoad;
                    if (oldPageCountToLoad < 0) {
                        return false;
                    }
                }
                sortedPages.put(page.getName(), page);
                new Thread(() -> controlListener.updateSize(sortedPages.size())).start();
                page.setParser(parser);
                executor.execute(page);
            } else {
                --countOfLinksInBase;
            }
            if (countOfLinksInBase > 0 && isOldLink) {
                countOfPagesToSkip = countOfLinksInBase / countOfLinksOnPage - 1;
                countOfLinksInBase = 0;
                return false;
            }
        }
        return true;
    }

    public void saveLink(Page page, boolean last) {
        baseHandler.appendBase(page.getLink(), last);
    }

    public List<Caption> getCaptions() {
        return captions;
    }

    private boolean checkDate(LocalDateTime date) {
        return lastDate.compareTo(date) < 0;
    }
}


