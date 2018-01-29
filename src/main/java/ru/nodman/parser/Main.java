package ru.nodman.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.controller.Controller;
import ru.nodman.parser.resources.Resources;
import ru.nodman.parser.view.View;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Resources.LOGGER_NAME);

    public static void main(String[] args) {
        LOG.info("-----------------------------------------------------------------");
        View view = new View();
        Controller controller = new Controller();
        controller.setViewListener(view);
        view.setControlListener(controller);
        view.startApplication();
    }
}
