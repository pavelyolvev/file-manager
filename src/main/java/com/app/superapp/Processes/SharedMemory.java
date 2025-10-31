package com.app.superapp.Processes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SharedMemory {
    static int size = 4096; // Размер разделяемой памяти в байтах
    static String filePath = System.getProperty("java.io.tmpdir") + "/sharedMemory.bin";// Путь к файлу разделяемой памяти
    static String split = "<->";
    static String pidSplit = "<--->";
    public static boolean send(ArrayList<Long> pids, int threadsAmount, String startTime) {
        try (FileChannel channel = getFileChannel()) {
            // Создание разделяемой памяти
            ByteBuffer sharedMemory = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);

            // Запись данных в разделяемую память
            StringBuilder pidBuilder = new StringBuilder();
            for (long pid : pids) {
                pidBuilder.append(pid).append(split);
            }
            pidBuilder.delete(pidBuilder.length()-split.length(), pidBuilder.length());
            String message = pidBuilder + pidSplit + threadsAmount + split + startTime + split;
            sharedMemory.put(message.getBytes(StandardCharsets.UTF_8));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getSplit() {
        return split;
    }
    public static String getPidSplit() {
        return pidSplit;
    }

    public static String receive() {
        try (FileChannel channel = getFileChannel()) {
            // Создание разделяемой памяти
            ByteBuffer sharedMemory = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);

            // Чтение данных из разделяемой памяти
            byte[] buffer = new byte[size];
            sharedMemory.rewind(); // Установка позиции на начало
            sharedMemory.get(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static FileChannel getFileChannel() throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        return file.getChannel();
    }
}
