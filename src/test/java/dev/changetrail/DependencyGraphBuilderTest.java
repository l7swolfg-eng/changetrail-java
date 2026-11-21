
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

}
