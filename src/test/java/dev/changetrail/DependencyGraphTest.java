
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
}
