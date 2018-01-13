package ru.nodman.parser.common;

public enum PagesCount {
    TEN("Загружать по 10 шт.", 1),
    TWENTY("Загружать по 50 шт.", 5),
    HUNDRED("Загружать по 100 шт.", 10),
    FIVE_HUNDRED("Загружать по 500 шт.", 50),
    THOUSAND("Загружать по 1000 шт.", 100),
    DATE("Загружать до даты...", -1);
    private String name;
    private int pagesCount;

    PagesCount(String name, int count) {
        this.name = name;
        this.pagesCount = count;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getCount() {
        return pagesCount;
    }
}
