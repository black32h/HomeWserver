package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5000;
    private static final int MAX_FILE_SIZE = 1024; // 1 KB

    public static void main(String[] args) {
        System.out.println("Сервер очікує підключень...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клієнт підключився.");

                try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    // Отримання метаданих файлу.
                    String fileName = in.readUTF();
                    int fileSize = in.readInt();

                    if (fileSize > MAX_FILE_SIZE) {
                        System.out.println("Файл занадто великий: " + fileSize + " байт.");
                        out.writeUTF("Файл не відповідає умовам (занадто великий).");
                        continue;
                    }

                    // Отримання файлу
                    byte[] fileData = new byte[fileSize];
                    in.readFully(fileData);

                    // Збереження файлу на сервері
                    File receivedFile = new File(fileName);
                    try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                        fos.write(fileData);
                    }
                    System.out.println("Файл '" + fileName + "' успішно збережено на сервері.");

                    // Відправлення відповіді клієнту
                    out.writeUTF("Файл '" + fileName + "' успішно збережено на сервері.");
                    out.writeInt(fileSize);
                    out.write(fileData);
                } catch (IOException e) {
                    System.err.println("Помилка під час обробки клієнта: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка запуску сервера: " + e.getMessage());
        }
    }
}
