package dev.changetrail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChangeTrailIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void reportsAffectedTestsForChangedFile() throws IOException {
        Path mainDir = Files.createDirectories(
                tempDir.resolve("src/main/java/com/shop")
        );

        Path testDir = Files.createDirectories(
                tempDir.resolve("src/test/java/com/shop")
        );

        Files.writeString(mainDir.resolve("PaymentService.java"), """
                package com.shop;

                public class PaymentService {
                }
                """);

        Files.writeString(mainDir.resolve("OrderService.java"), """
                package com.shop;

                import com.shop.PaymentService;

                public class OrderService {
                }
                """);

        Files.writeString(testDir.resolve("OrderServiceTest.java"), """
                package com.shop;

                import com.shop.OrderService;

                public class OrderServiceTest {
                }
                """);

        Files.writeString(testDir.resolve("OtherServiceTest.java"), """
                package com.shop;

                public class OtherServiceTest {
                }
                """);

        // Capture the output produced by Main.
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(
                    output, true, StandardCharsets.UTF_8
            ));

            Main.main(new String[]{
                    tempDir.toString(),
                    "src/main/java/com/shop/PaymentService.java"
            });
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("OrderService.java"));
        assertTrue(result.contains("OrderServiceTest.java"));
        assertFalse(result.contains("OtherServiceTest.java"));
        assertTrue(result.contains("Total affected tests: 1"));
    }

    @Test
    void includesChangedTestFileInAffectedTests() throws IOException {
        Path testDir = Files.createDirectories(
                tempDir.resolve("src/test/java/com/shop")
        );

        Files.writeString(testDir.resolve("OrderServiceTest.java"), """
                package com.shop;

                public class OrderServiceTest {
                }
                """);

        // A minimal production directory is required by buildProject().
        Files.createDirectories(tempDir.resolve("src/main/java"));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(
                    output, true, StandardCharsets.UTF_8
            ));

            Main.main(new String[]{
                    tempDir.toString(),
                    "src/test/java/com/shop/OrderServiceTest.java"
            });
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Affected test files:"));
        assertTrue(result.contains("  src/test/java/com/shop/OrderServiceTest.java"));
        assertTrue(result.contains("Total affected tests: 1"));
    }

}
