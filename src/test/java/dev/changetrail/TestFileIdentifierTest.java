package dev.changetrail;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestFileIdentifierTest {

    @Test
    void recognizesJavaTestFiles() {
        Path file = Path.of(
                "project/src/test/java/com/shop/OrderServiceTest.java"
        );

        assertTrue(TestFileIdentifier.isTestFile(file));
    }

    @Test
    void ignoresProductionFiles() {
        Path file = Path.of(
                "project/src/main/java/com/shop/OrderService.java"
        );

        assertFalse(TestFileIdentifier.isTestFile(file));
    }

    @Test
    void ignoresNonJavaFiles() {
        Path file = Path.of(
                "project/src/test/java/config.txt"
        );

        assertFalse(TestFileIdentifier.isTestFile(file));
    }
}
