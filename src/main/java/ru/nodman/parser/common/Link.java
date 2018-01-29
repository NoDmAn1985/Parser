package ru.nodman.parser.common;

import java.time.LocalDateTime;

public class Link {
    private String name;
    private String address;
    private LocalDateTime date;

    public Link(String name, String address, LocalDateTime date) {
        this.name = name.replaceAll("'|\"", "");
        this.address = address;
        this.date = date;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        if (!name.equals(link.name)) {
            return false;
        }
        if (!address.equals(link.address)) {
            return false;
        }
        return date.equals(link.date);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name + " (" + address + "), " + date;
    }
}
