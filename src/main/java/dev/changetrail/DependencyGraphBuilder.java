
package dev.changetrail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyGraphBuilder {

    public static DependencyGraph build(Path sourceRoot) throws IOException {

        List<Path> files = JavaFileScanner.findJavaFiles(sourceRoot);

        DependencyGraph graph = new DependencyGraph();
        Map<String, Path> classFiles = new HashMap<>();

        // First pass: map class names to their source files.
        for (Path file : files) {
            graph.addFile(file);

            Path relative = sourceRoot.relativize(file);
            String className = relative.toString()
                    .replace('/', '.')
                    .replace('\\', '.')
                    .replaceAll("\\.java$", "");

            classFiles.put(className, file);
        }

        // Second pass: connect files using their imports.
        for (Path file : files) {
            for (String imported : JavaImportScanner.findImports(file)) {

                Path dependency = classFiles.get(imported);

                if (dependency != null && !dependency.equals(file)) {
                    graph.addDependency(file, dependency);
                }
            }
        }

        return graph;
    }
}
