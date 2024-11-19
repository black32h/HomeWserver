package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

                    // Отримання метаданих файлу
                    String fileName = in.readUTF();
                    int fileSize = in.readInt();
                    String clientHash = in.readUTF();

                    if (fileSize > MAX_FILE_SIZE) {
                        System.out.println("Файл занадто великий: " + fileSize + " байт.");
                        out.writeUTF("Файл не відповідає умовам (занадто великий).");
                        continue;
                    }

                    // Отримання файлу
                    byte[] fileData = new byte[fileSize];
                    in.readFully(fileData);

                    // Розрахунок хешу на сервері
                    String serverHash = calculateHash(fileData);
                    if (!serverHash.equals(clientHash)) {
                        System.out.println("Цілісність файлу порушена.");
                        out.writeUTF("Цілісність файлу порушена.");
                        continue;
                    }

                    // Збереження файлу
                    File receivedFile = new File(fileName);
                    try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                        fos.write(fileData);
                    }
                    System.out.println("Файл '" + fileName + "' успішно збережено.");

                    // Відправлення файлу назад клієнту
                    out.writeUTF("Файл '" + fileName + "' успішно збережено.");
                    out.writeUTF(serverHash);
                    out.writeInt(fileSize);
                    out.write(fileData);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.err.println("Помилка під час обробки клієнта: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка запуску сервера: " + e.getMessage());
        }
    }

    // Метод для обчислення SHA-256 хешу
    private static String calculateHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);
        StringBuilder hashString = new StringBuilder();
        for (byte b : hashBytes) {
            hashString.append(String.format("%02x", b));
        }
        return hashString.toString();
    }
}
