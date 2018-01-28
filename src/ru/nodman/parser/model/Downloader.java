package ru.nodman.parser.model;

import ru.nodman.parser.resources.Resources;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

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
        String fileName = Resources.DOWNLOAD_PATH + name;
        file = new File(fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println(fileName);
                e.printStackTrace();
            }
            file.deleteOnExit();
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(goodLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

//            connection.setRequestProperty("Referer", "google.com");
            String host = url.getHost();
            String serverName = host.substring(host.lastIndexOf('.', host.length() - 5) + 1);
            connection.setRequestProperty("Referer", "http://" + serverName + "/");
//            connection.setRequestProperty("User-Agent", "Mozilla");

            connection.setUseCaches(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                InputStream in = connection.getInputStream();
                OutputStream writer = new FileOutputStream(fileName);
                byte buffer[] = new byte[1024];
                int c = in.read(buffer);
                while (c > 0) {
                    writer.write(buffer, 0, c);
                    c = in.read(buffer);
                }
                writer.flush();
                writer.close();
                in.close();
            } else if (HttpURLConnection.HTTP_MOVED_PERM == connection.getResponseCode()) {
                String newLink = connection.getHeaderField("Location");
//                System.out.println("РЕДИРЕКТ: " + goodLink + " на " + newLink);
                download(newLink);
            } else {
//                System.out.println("Fail ( " + goodLink + " ), " + connection.getResponseCode() + ", " + connection.getResponseMessage());
            }
        } catch (IOException e) {
//            System.out.println("Беда с загрузкой файла: " + goodLink);
//            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return file;
    }
}
