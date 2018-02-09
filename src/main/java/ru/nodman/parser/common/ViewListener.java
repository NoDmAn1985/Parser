package ru.nodman.parser.common;

public interface ViewListener {
    void setSize(int pageNumber, int size);

    void updateTitle(String text);

    void showErrorMsg();
}
