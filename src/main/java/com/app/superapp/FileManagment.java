package com.app.superapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileManagment {
    public static void copy(Path sourceDirectory, Path targetDirectory) throws IOException {

        Path target = Path.of(targetDirectory + "/" + sourceDirectory.toFile().getName());
        if(sourceDirectory.toFile().isFile()){
            try {
                Files.move(sourceDirectory, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // Создаем целевую директорию, если она не существует
        if (!Files.exists(Path.of(targetDirectory + "/" + sourceDirectory.toFile().getName()))) {
            Files.createDirectories(Path.of(targetDirectory + "/" + sourceDirectory.toFile().getName()));
        }
        Files.walk(sourceDirectory, 1).filter(path -> !path.equals(sourceDirectory)).forEach(sourcePath -> {

            Path targetPath = target.resolve(sourcePath.getFileName());
            try {
                if (Files.isDirectory(sourcePath)) {
                    copy(sourcePath, targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    public static void rename(File file, String newName){
        file.renameTo(new File(file.getParent() + "/" + newName));
    }
    public static void move(Path src, Path dest) {
        src.toFile().renameTo(new File(dest + "/" + src.toFile().getName()));
    }
    public static void delete(File file){
        if (!Files.exists(file.toPath())) {
            return;
        }
        if(file.isFile()){
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        // Рекурсивно удаляем все файлы и поддиректории
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(file.toPath())) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    delete(path.toFile());
                } else {
                    Files.delete(path);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Удаляем саму директорию
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
