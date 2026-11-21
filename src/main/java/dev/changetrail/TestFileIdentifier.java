package dev.changetrail;

import java.nio.file.Path;

public class TestFileIdentifier {

    public static boolean isTestFile(Path file) {

        if (file.getFileName() == null ||
                !file.toString().endsWith(".java")) {
            return false;
        }

        Path normalized = file.normalize();

        // Check whether the file is inside src/test/java.
        for (int i = 0; i < normalized.getNameCount() - 2; i++) {
            if (normalized.getName(i).toString().equals("src") &&
                    normalized.getName(i + 1).toString().equals("test") &&
                    normalized.getName(i + 2).toString().equals("java")) {
                return true;
            }
        }

        return false;
    }
}
