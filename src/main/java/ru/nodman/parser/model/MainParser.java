package ru.nodman.parser.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.Main;
import ru.nodman.parser.common.Caption;
import ru.nodman.parser.common.ControlListener;
import ru.nodman.parser.common.Page;
import ru.nodman.parser.common.ParserMode;
import ru.nodman.parser.model.parsers.Parser;
import ru.nodman.parser.resources.Resources;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainParser {
    private static final Logger LOG = LoggerFactory.getLogger(MainParser.class.getSimpleName());

    private static final long SLEEPING_TIME = 5000;
    private TreeMap<String, Page> sortedPages;
    private ControlListener controlListener;
    private BaseHandler baseHandler;
    private int oldPageCountToLoad;
    private int infoPagesCount;
    private LocalDateTime lastDate;
    private int countOfLinksOnPage;
    private int countOfLinksInBase;
    private int countOfPagesToSkip;
    private List<Caption> captions;
    private ExecutorService executor;
    private Parser parser;
    private boolean isEnd = false;


    public MainParser() {
        captions = BaseHandler.loadCaptions();
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }

    public void parse(Caption caption, List<Page> pages) throws InterruptedException {
        baseHandler = new BaseHandler(Resources.TIME_ZONE);
        new Thread(baseHandler).start();

        oldPageCountToLoad = Resources.OLD_PAGES_COUNT;
        infoPagesCount = Resources.INFO_PAGES_COUNT;

        baseHandler.loadBase(caption);
        countOfLinksInBase = baseHandler.getBaseSize();

        executor = Executors.newFixedThreadPool(Resources.THREAD_COUNT);

        sortedPages = new TreeMap<>();
        controlListener.updateSize(sortedPages.size());

        String url = caption.getAddress();
        String urlReserve = caption.getSecondAddress();
        lastDate = caption.getDate();
        LOG.debug("последняя дата - {}", lastDate);
        int index = caption.getFirstPage();
        String pagePattern = caption.getPagePattern();

        try {
            parser = setParser(caption);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            LOG.error("ошибка создания парсера, {}", e);
        }

        isEnd = false;
        while (!isEnd) {
            Deque<Page> tempListOfPage;
            String address = url + String.format(pagePattern, index);
            LOG.debug("смотрю страницу - {}", address);
            try {
                tempListOfPage = parser.parseUrl(address);
            } catch (IOException | IndexOutOfBoundsException e) {
                LOG.error("не смог распарсить address = {}, {}", address, e);
                url = urlReserve;
                address = url + String.format(pagePattern, index);
                LOG.debug("смотрю страницу - {}", address);
                try {
                    tempListOfPage = parser.parseUrl(address);
                } catch (IOException ex) {
                    LOG.error("не смог распарсить address = {}, {}", address, ex);
                    isEnd = false;
                    break;
                }
            }

            if (countOfLinksOnPage == 0) {
                controlListener.changeParserMode(ParserMode.PARSING);
                countOfLinksOnPage = tempListOfPage.size();
            }

            boolean isWrongPages = !addPages(tempListOfPage);
            if (isWrongPages) {
                if (countOfPagesToSkip > 0) {
                    LOG.debug("пропускаю страницы, осталось - {}", countOfPagesToSkip);
                    index += countOfPagesToSkip;
                    countOfPagesToSkip = 0;
                } else if (oldPageCountToLoad < 0) {
                    LOG.debug("загруженно достаточно старых страниц ({})", Resources.OLD_PAGES_COUNT);
                    isEnd = true;
                }
            }
            ++index;
        }
        pages.addAll(sortedPages.values());
        controlListener.updateSize(sortedPages.size());

        Thread.sleep(SLEEPING_TIME);
    }

    private Parser setParser(Caption caption) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String parserName = caption.getParserName();
        String captionUrl = caption.getUrl();

        String className = "ru.nodman.parser.model.parsers." + parserName;
        Class<?> cls = Class.forName(className);
        if (!Parser.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException();
        }
        return (Parser) cls
                .getConstructor(BaseHandler.class, String.class)
                .newInstance(baseHandler, captionUrl);
    }

    private boolean addPages(Deque<Page> tempListOfPage) {
        for (Page page : tempListOfPage) {
            boolean isOldLink = !isNewLink(page.getDate());
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
                infoPagesCount = (isOldLink ? --infoPagesCount : infoPagesCount);
                --countOfLinksInBase;
                if (countOfLinksInBase > 0 && isOldLink && infoPagesCount <= 0) {
                    countOfPagesToSkip = countOfLinksInBase / countOfLinksOnPage - 1;
                    countOfLinksInBase = 0;
                    return false;
                }
            }
        }
        return true;
    }

    public void saveLink(Page page, boolean last) {
        baseHandler.appendBase(page.getLink(), isEnd && last);
    }

    public List<Caption> getCaptions() {
        return captions;
    }

    private boolean isNewLink(LocalDateTime date) {
        return date == null || lastDate.compareTo(date) < 0;
    }
}


