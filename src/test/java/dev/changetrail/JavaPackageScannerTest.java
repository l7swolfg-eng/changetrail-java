package dev.changetrail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JavaPackageScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void findsPackageDeclaration() throws IOException {
        Path file = tempDir.resolve("PaymentService.java");

        Files.writeString(file, """
                package com.shop.services;

                public class PaymentService {
                }
                """);

        Optional<String> result = JavaPackageScanner.findPackage(file);

        assertEquals(Optional.of("com.shop.services"), result);
    }

    @Test
    void handlesDefaultPackage() throws IOException {
        Path file = tempDir.resolve("Main.java");

        Files.writeString(file, """
                public class Main {
                }
                """);

        Optional<String> result = JavaPackageScanner.findPackage(file);

        assertEquals(Optional.empty(), result);
    }
}
