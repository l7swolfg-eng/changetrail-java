package dev.changetrail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaFileScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void findsJavaFilesInNestedDirectories() throws IOException {
        Path nested = Files.createDirectories(tempDir.resolve("src/services"));

        Path first = Files.createFile(tempDir.resolve("Main.java"));
        Path second = Files.createFile(nested.resolve("PaymentService.java"));
        Files.createFile(tempDir.resolve("README.md"));

        List<Path> results = JavaFileScanner.findJavaFiles(tempDir);

        assertEquals(2, results.size());
        assertTrue(results.contains(first));
        assertTrue(results.contains(second));
    }

    @Test
    void rejectsInvalidDirectories() {
        Path missing = tempDir.resolve("missing");

        assertThrows(IllegalArgumentException.class,
                () -> JavaFileScanner.findJavaFiles(missing));
    }
}
