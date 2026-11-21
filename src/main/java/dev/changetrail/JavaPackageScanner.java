package dev.changetrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class JavaPackageScanner {

    public static Optional<String> findPackage(Path javaFile) throws IOException {

        for (String line : Files.readAllLines(javaFile)) {
            String trimmed = line.trim();

            if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                String packageName = trimmed.substring(
                        8, trimmed.length() - 1
                ).trim();

                return Optional.of(packageName);
            }
        }

        return Optional.empty();
    }
}
