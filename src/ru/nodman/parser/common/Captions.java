package ru.nodman.parser.common;

public enum Captions {
    MAIN("Главная", "http://hdreactor.club/", "Hdreactor"),
    PAGE_1("4K", "http://hdreactor.club/4k_uhd/", "Hdreactor"),
    PAGE_2("3D", "http://hdreactor.club/tags/3D/", "Hdreactor"),
    PAGE_3("Кино", "http://hdreactor.club/1/", "Hdreactor"),
    PAGE_4("Сериалы", "http://hdreactor.club/6/", "Hdreactor"),
    PAGE_5("Мульты", "http://hdreactor.club/7/", "Hdreactor"),
    PAGE_6("Музыка", "http://hdreactor.club/4/", "Hdreactor"),
    PAGE_7("Игры", "http://hdreactor.club/5/", "Hdreactor"),
    PAGE_8(" 18+", "http://hdreactor.club/3/", "Hdreactor"),
    PAGE_9("Спорт", "http://hdreactor.club/sport-hd/", "Hdreactor");

    private String caption;
    private String url;
    private String name;

    Captions(String caption, String url, String name) {
        this.caption = caption;
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return caption;
    }
}
