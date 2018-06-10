package ru.nodman.parser.common;

public interface ControlListener {
    Page parse(Caption caption);

    Page getNext();

    Page getPrevious();

    void updateSize(int size);

    Caption[] getCaptions();

    void changeParserMode(ParserMode mode);
}
