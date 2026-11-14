package dev.changetrail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        Path directory = args.length > 0
                ? Path.of(args[0])
                : Path.of(".");

        List<Path> javaFiles = JavaFileScanner.findJavaFiles(directory);

        System.out.println("Found " + javaFiles.size() + " Java files:");

        for (Path file : javaFiles) {
            System.out.println(directory.relativize(file));
        }
    }
}
