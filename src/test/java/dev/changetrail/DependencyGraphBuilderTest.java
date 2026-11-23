
package dev.changetrail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DependencyGraphBuilderTest {

    @TempDir
    Path tempDir;

    @Test
    void buildsGraphFromImports() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");
        Path packageDir = Files.createDirectories(
                sourceRoot.resolve("com/shop")
        );

        Path payment = packageDir.resolve("PaymentService.java");
        Path order = packageDir.resolve("OrderService.java");
        Path orderTest = packageDir.resolve("OrderServiceTest.java");

        Files.writeString(payment, """
                package com.shop;

                public class PaymentService {
                }
                """);

        Files.writeString(order, """
                package com.shop;

                import com.shop.PaymentService;

                public class OrderService {
                }
                """);

        Files.writeString(orderTest, """
                package com.shop;

                import com.shop.OrderService;

                public class OrderServiceTest {
                }
                """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertEquals(Set.of(order), graph.getDependents(payment));
        assertEquals(Set.of(orderTest), graph.getDependents(order));

        // Changing PaymentService should also affect OrderServiceTest.
        assertEquals(
                Set.of(order, orderTest),
                graph.getAffectedFiles(payment)
        );
    }

    @Test
    void ignoresExternalImports() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");
        Path packageDir = Files.createDirectories(
                sourceRoot.resolve("com/example")
        );

        Path example = packageDir.resolve("Example.java");

        Files.writeString(example, """
                package com.example;

                import java.util.List;

                public class Example {
                }
                """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertTrue(graph.getDependents(example).isEmpty());
        assertTrue(graph.getAffectedFiles(example).isEmpty());
    }

    @Test
    void resolvesDependenciesUsingDeclaredPackages() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");

        Path servicesDir = Files.createDirectories(
                sourceRoot.resolve("services")
        );

        Path applicationDir = Files.createDirectories(
                sourceRoot.resolve("application")
        );

        Path payment = servicesDir.resolve("PaymentService.java");
        Path order = applicationDir.resolve("OrderService.java");

        Files.writeString(payment, """
            package com.shop.payments;

            public class PaymentService {
            }
            """);

        Files.writeString(order, """
            package com.shop.orders;

            import com.shop.payments.PaymentService;

            public class OrderService {
            }
            """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertEquals(Set.of(order), graph.getDependents(payment));
        assertEquals(Set.of(order), graph.getAffectedFiles(payment));
    }

    @Test
    void connectsProductionFilesToTestFiles() throws IOException {
        Path mainRoot = tempDir.resolve("src/main/java");
        Path testRoot = tempDir.resolve("src/test/java");

        Path paymentDir = Files.createDirectories(
                mainRoot.resolve("com/shop/payments")
        );

        Path orderDir = Files.createDirectories(
                mainRoot.resolve("com/shop/orders")
        );

        Path testDir = Files.createDirectories(
                testRoot.resolve("com/shop/tests")
        );

        Path payment = paymentDir.resolve("PaymentService.java");
        Path order = orderDir.resolve("OrderService.java");
        Path orderTest = testDir.resolve("OrderServiceTest.java");

        Files.writeString(payment, """
            package com.shop.payments;

            public class PaymentService {
            }
            """);

        Files.writeString(order, """
            package com.shop.orders;

            import com.shop.payments.PaymentService;

            public class OrderService {
            }
            """);

        Files.writeString(orderTest, """
            package com.shop.tests;

            import com.shop.orders.OrderService;

            public class OrderServiceTest {
            }
            """);

        DependencyGraph graph = DependencyGraphBuilder.buildProject(tempDir);

        Set<Path> affected = graph.getAffectedFiles(payment);

        assertEquals(Set.of(order, orderTest), affected);

        // Only the test source should be classified as a test file.
        assertFalse(TestFileIdentifier.isTestFile(order));
        assertTrue(TestFileIdentifier.isTestFile(orderTest));
    }

    @Test
    void detectsSamePackageDependencies() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");
        Path packageDir = Files.createDirectories(
                sourceRoot.resolve("com/shop")
        );

        Path payment = packageDir.resolve("PaymentService.java");
        Path order = packageDir.resolve("OrderService.java");
        Path unrelated = packageDir.resolve("Logger.java");

        Files.writeString(payment, """
            package com.shop;

            public class PaymentService {
            }
            """);

        Files.writeString(order, """
            package com.shop;

            public class OrderService {
                private PaymentService payment;
            }
            """);

        Files.writeString(unrelated, """
            package com.shop;

            public class Logger {
            }
            """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertEquals(Set.of(order), graph.getDependents(payment));
        assertEquals(Set.of(order), graph.getAffectedFiles(payment));
    }


    @Test
    void ignoresClassNamesInCommentsAndStrings() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");
        Path packageDir = Files.createDirectories(
                sourceRoot.resolve("com/shop")
        );

        Path payment = packageDir.resolve("PaymentService.java");
        Path audit = packageDir.resolve("AuditLog.java");

        Files.writeString(payment, """
            package com.shop;

            public class PaymentService {
            }
            """);

        Files.writeString(audit, """
            package com.shop;

            public class AuditLog {
                // PaymentService handles transactions.
                String message = "PaymentService";
            }
            """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertTrue(graph.getDependents(payment).isEmpty());
        assertTrue(graph.getAffectedFiles(payment).isEmpty());
    }



    @Test
    void connectsFilesUsingStaticImports() throws IOException {
        Path sourceRoot = tempDir.resolve("src/main/java");
        Path configDir = Files.createDirectories(
                sourceRoot.resolve("com/shop/config")
        );
        Path serviceDir = Files.createDirectories(
                sourceRoot.resolve("com/shop/service")
        );

        Path constants = configDir.resolve("Constants.java");
        Path order = serviceDir.resolve("OrderService.java");
        Path report = serviceDir.resolve("ReportService.java");

        Files.writeString(constants, """
                package com.shop.config;

                public class Constants {
                    public static final int MAX_RETRIES = 3;
                }
                """);

        Files.writeString(order, """
                package com.shop.service;

                import static com.shop.config.Constants.MAX_RETRIES;

                public class OrderService {
                    int retries = MAX_RETRIES;
                }
                """);

        Files.writeString(report, """
                package com.shop.service;

                import static com.shop.config.Constants.*;

                public class ReportService {
                    int retries = MAX_RETRIES;
                }
                """);

        DependencyGraph graph = DependencyGraphBuilder.build(sourceRoot);

        assertEquals(Set.of(order, report), graph.getDependents(constants));
        assertEquals(Set.of(order, report), graph.getAffectedFiles(constants));
    }

}
