package dev.changetrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class JavaFileScanner {

    public static List<Path> findJavaFiles(Path directory) throws IOException {

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Invalid directory: " + directory);
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }
}
