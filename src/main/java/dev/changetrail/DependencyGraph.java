
package dev.changetrail;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {

    // Maps each file to the files that depend on it.
    private final Map<Path, Set<Path>> dependents = new HashMap<>();

    public void addFile(Path file) {
        dependents.putIfAbsent(file.normalize(), new HashSet<>());
    }

    public void addDependency(Path dependent, Path dependency) {
        Path dependentFile = dependent.normalize();
        Path dependencyFile = dependency.normalize();

        addFile(dependentFile);
        addFile(dependencyFile);

        dependents.get(dependencyFile).add(dependentFile);
    }

    public Set<Path> getDependents(Path file) {
        return Set.copyOf(
                dependents.getOrDefault(file.normalize(), Set.of())
        );
    }
}
