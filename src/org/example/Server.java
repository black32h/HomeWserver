package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5000;
    private static final String OUTPUT_FILE = "received_file.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущено на порту " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    System.out.println("Підключено клієнта.");

                    File file = new File(OUTPUT_FILE);
                    int lastBlockReceived = 0;

                    if (file.exists()) {
                        lastBlockReceived = readProgress();
                    }

                    out.writeInt(lastBlockReceived); // Надсилаємо клієнту останній отриманий блок
                    System.out.println("Останній отриманий блок: " + lastBlockReceived);

                    try (FileOutputStream fos = new FileOutputStream(file, true)) {
                        while (true) {
                            int blockNumber = in.readInt();
                            int blockSize = in.readInt();

                            byte[] buffer = new byte[blockSize];
                            in.readFully(buffer);

                            fos.write(buffer);
                            saveProgress(blockNumber);
                            out.writeUTF("BLOCK_RECEIVED:" + blockNumber); // Підтвердження отримання блоку
                            System.out.println("Отримано блок " + blockNumber);
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Передача завершена.");
                } catch (IOException e) {
                    System.err.println("Помилка сервера: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Не вдалося запустити сервер: " + e.getMessage());
        }
    }

    private static void saveProgress(int lastBlock) throws IOException {
        try (FileWriter writer = new FileWriter("progress.txt")) {
            writer.write(String.valueOf(lastBlock));
        }
    }

    private static int readProgress() {
        try (BufferedReader reader = new BufferedReader(new FileReader("progress.txt"))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            return 0;
        }
    }
}
