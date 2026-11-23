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
    void resolvesStaticImportsToOwningClasses() throws IOException {
        Path javaFile = tempDir.resolve("Calculator.java");

        String source = """
                import static java.lang.Math.PI;
                import static com.example.Constants.MAX_RETRIES;
                import static com.example.Helpers.*;
                import java.util.ArrayList;

                public class Calculator {
                }
                """;

        Files.writeString(javaFile, source);

        List<String> imports = JavaImportScanner.findImports(javaFile);

        assertEquals(List.of(
                "java.lang.Math",
                "com.example.Constants",
                "com.example.Helpers",
                "java.util.ArrayList"
        ), imports);
    }

    @Test
    void returnsEmptyListWhenNoImportsExist() throws IOException {
        Path javaFile = tempDir.resolve("Main.java");

        Files.writeString(javaFile, "public class Main {}");

        List<String> imports = JavaImportScanner.findImports(javaFile);

        assertTrue(imports.isEmpty());
    }

    @Test
    void ignoresImportsInsideBlockComments() throws IOException {
        Path file = tempDir.resolve("OrderService.java");

        Files.writeString(file, """
            package com.shop;

            /*
            import com.shop.PaymentService;
            */

            import java.util.List;

            public class OrderService {
            }
            """);

        List<String> imports = JavaImportScanner.findImports(file);

        assertEquals(List.of("java.util.List"), imports);
    }

    @Test
    void ignoresImportsInsideTextBlocks() throws IOException {
        Path file = tempDir.resolve("Example.java");

        String source =
                "public class Example {\n" +
                        "    String text = \"\"\"\n" +
                        "import com.shop.PaymentService;\n" +
                        "\"\"\";\n" +
                        "}\n";

        Files.writeString(file, source);

        List<String> imports = JavaImportScanner.findImports(file);

        assertTrue(imports.isEmpty());
    }

}
