package io.github.evercraftmc.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FileDeleteHook {
    private FileDeleteHook() {
    }

    static {
        Thread shutdownThread = new Thread(FileDeleteHook::deleteAll, "FileDeleteHook Cleanup");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private static final @NotNull List<Path> toDelete = new ArrayList<>();

    public static void add(@NotNull Path path) {
        toDelete.add(path);
    }

    static void deleteAll() {
        for (Path path : toDelete) {
            try {
                Files.walkFileTree(path, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        toDelete.clear();
    }
}