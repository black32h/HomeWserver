package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущено на порту: " + PORT);

            while (true) {
                System.out.println("Очікування підключення клієнта...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клієнт підключився: " + clientSocket);

                try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    // Отримання метаданих файлу
                    String fileName = in.readUTF();
                    int fileSize = in.readInt();

                    System.out.println("Отримано файл: " + fileName + " (Розмір: " + fileSize + " байт)");

                    if (fileSize <= 1024) {
                        // Отримання файлу
                        byte[] fileData = new byte[fileSize];
                        in.readFully(fileData);

                        // Збереження файлу на сервері
                        File file = new File(fileName);
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(fileData);
                            System.out.println("Файл збережено на сервері: " + file.getAbsolutePath());
                        }

                        // Відправлення файлу назад клієнту
                        out.writeUTF("Файл успішно отримано та збережено.");
                        out.writeInt(fileSize);
                        out.write(fileData);
                        System.out.println("Файл відправлено назад клієнту.");
                    } else {
                        out.writeUTF("Файл занадто великий. Повинен бути ≤ 1 КБ.");
                        System.out.println("Файл не задовольняє умовам.");
                    }
                } catch (IOException e) {
                    System.err.println("Помилка під час обробки файлу: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка запуску сервера: " + e.getMessage());
        }
    }
}
