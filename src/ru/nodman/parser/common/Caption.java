package ru.nodman.parser.common;

import java.time.LocalDateTime;
import java.util.Date;

public class Caption {
    private String name;
    private String address;
    private LocalDateTime date;
    private String baseName;
    private int firstPage;
    private String pagePattern;
    private String linkAttr;
    private String linkAttrValue;
    private String nameTag;
    private String queryForDate;
    private String previousElementText;
    private String parserName;

    public Caption() {
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setFirstPage(int firstPage) {
        this.firstPage = firstPage;
    }

    public void setPagePattern(String pagePattern) {
        this.pagePattern = pagePattern;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public String getPagePattern() {
        return pagePattern;
    }

    public void setLinkAttr(String linkAttr) {
        this.linkAttr = linkAttr;
    }

    public void setLinkAttrValue(String linkAttrValue) {
        this.linkAttrValue = linkAttrValue;
    }

    public String getLinkAttr() {
        return linkAttr;
    }

    public String getLinkAttrValue() {
        return linkAttrValue;
    }

    public void setNameTag(String nameTag) {
        this.nameTag = nameTag;
    }

    public String getNameTag() {
        return nameTag;
    }

    public void setQueryForDate(String queryForDate) {
        this.queryForDate = queryForDate;
    }

    public void setPreviousElementText(String previousElementText) {
        this.previousElementText = previousElementText;
    }

    public String getQueryForDate() {
        return queryForDate;
    }

    public String getPreviousElementText() {
        return previousElementText;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public String getParserName() {
        return parserName;
    }
}
