package ru.nodman.parser.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nodman.parser.resources.Resources;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class.getSimpleName());

    private Downloader() {
        // for SonarLint
    }

    public static File download(String link) {
        File file;
        String goodLink;
        if (link.charAt(link.length() - 1) == '/' ||
                link.charAt(link.length() - 1) == '?') {
            goodLink = link.substring(0, link.length() - 1);
        } else {
            goodLink = link;
        }

        String name = goodLink.substring(goodLink.lastIndexOf('/') + 1);
        String fileName = System.getProperty("java.io.tmpdir") + Resources.DOWNLOAD_PATH + name;
        file = new File(fileName);
        if (!file.exists()) {
            boolean isDirectoryCreate = file.getParentFile().mkdirs();
            boolean isFileCreated = false;
            try {
                isFileCreated = file.createNewFile();
            } catch (IOException e) {
                LOG.error("не удалось создать файл {}, {}", fileName, e);
            }
            file.deleteOnExit();
            LOG.debug("isDirectoryCreate && isFileCreated = {}", isDirectoryCreate && isFileCreated);
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(goodLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            String host = url.getHost();
            String serverName = host.substring(host.lastIndexOf('.', host.length() - 5) + 1);
            connection.setRequestProperty("Referer", "http://" + serverName + "/");

            connection.setUseCaches(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                try (InputStream in = connection.getInputStream();
                     OutputStream writer = new FileOutputStream(fileName)) {
                    byte[] buffer = new byte[1024];
                    int c = in.read(buffer);
                    while (c > 0) {
                        writer.write(buffer, 0, c);
                        c = in.read(buffer);
                    }
                }
            } else if (HttpURLConnection.HTTP_MOVED_PERM == connection.getResponseCode()) {
                String newLink = connection.getHeaderField("Location");
                download(newLink);
            }
        } catch (IOException e) {
            LOG.error("Беда с загрузкой файла: {}, e", goodLink, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return file;
    }
}
