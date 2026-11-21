
package dev.changetrail;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DependencyGraphTest {

    @Test
    void storesDependencies() {
        DependencyGraph graph = new DependencyGraph();

        Path payment = Path.of("PaymentService.java");
        Path order = Path.of("OrderService.java");

        graph.addDependency(order, payment);

        assertEquals(Set.of(order), graph.getDependents(payment));
        assertTrue(graph.getDependents(order).isEmpty());
    }

    @Test
    void preventsDuplicateDependencies() {
        DependencyGraph graph = new DependencyGraph();

        Path payment = Path.of("PaymentService.java");
        Path order = Path.of("OrderService.java");

        graph.addDependency(order, payment);
        graph.addDependency(order, payment);

        assertEquals(1, graph.getDependents(payment).size());
    }

    @Test
    void handlesFilesWithNoDependents() {
        DependencyGraph graph = new DependencyGraph();

        Path file = Path.of("Main.java");
        graph.addFile(file);

        assertTrue(graph.getDependents(file).isEmpty());
    }

    @Test
    void findsDirectAndIndirectDependents() {
        DependencyGraph graph = new DependencyGraph();

        Path payment = Path.of("PaymentService.java");
        Path order = Path.of("OrderService.java");
        Path orderTest = Path.of("OrderServiceTest.java");

        graph.addDependency(order, payment);
        graph.addDependency(orderTest, order);

        Set<Path> affected = graph.getAffectedFiles(payment);

        assertEquals(Set.of(order, orderTest), affected);
    }

    @Test
    void handlesCircularDependencies() {
        DependencyGraph graph = new DependencyGraph();

        Path fileA = Path.of("A.java");
        Path fileB = Path.of("B.java");

        graph.addDependency(fileA, fileB);
        graph.addDependency(fileB, fileA);

        Set<Path> affected = graph.getAffectedFiles(fileA);

        assertEquals(Set.of(fileB), affected);
    }

    @Test
    void excludesUnrelatedFiles() {
        DependencyGraph graph = new DependencyGraph();

        Path payment = Path.of("PaymentService.java");
        Path order = Path.of("OrderService.java");
        Path unrelated = Path.of("Logger.java");

        graph.addDependency(order, payment);
        graph.addFile(unrelated);

        Set<Path> affected = graph.getAffectedFiles(payment);

        assertEquals(Set.of(order), affected);
        assertFalse(affected.contains(unrelated));
    }
}
