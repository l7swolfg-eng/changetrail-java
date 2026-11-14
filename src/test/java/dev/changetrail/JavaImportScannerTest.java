
package dev.changetrail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaImportScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void findsRegularImports() throws IOException {
        Path javaFile = tempDir.resolve("OrderService.java");

        String source = """
                package com.example;

                import java.util.List;
                import com.example.services.PaymentService;

                public class OrderService {
                }
                """;

        Files.writeString(javaFile, source);

        List<String> imports = JavaImportScanner.findImports(javaFile);

        assertEquals(2, imports.size());
        assertTrue(imports.contains("java.util.List"));
        assertTrue(imports.contains("com.example.services.PaymentService"));
    }

    @Test
    void ignoresStaticImports() throws IOException {
        Path javaFile = tempDir.resolve("Calculator.java");

        String source = """
                import static java.lang.Math.PI;
                import java.util.ArrayList;

                public class Calculator {
                }
                """;

        Files.writeString(javaFile, source);

        List<String> imports = JavaImportScanner.findImports(javaFile);

        assertEquals(List.of("java.util.ArrayList"), imports);
    }

    @Test
    void returnsEmptyListWhenNoImportsExist() throws IOException {
        Path javaFile = tempDir.resolve("Main.java");

        Files.writeString(javaFile, "public class Main {}");

        List<String> imports = JavaImportScanner.findImports(javaFile);

        assertTrue(imports.isEmpty());
    }
}
