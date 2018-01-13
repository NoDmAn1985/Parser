package ru.nodman.parser;

import ru.nodman.parser.controller.Controller;
import ru.nodman.parser.view.View;

public class Main {
    public static void main(String[] args) {
        View view = new View();
        Controller controller = new Controller();
        controller.setViewListener(view);
        view.setControlListener(controller);
        view.startApplication();
    }
}
