
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


// First pass: identify each class using its declared package.
        for (Path file : files) {
            graph.addFile(file);

            String fileName = file.getFileName().toString();
            String simpleName = fileName.substring(0, fileName.length() - 5);

            String packageName = JavaPackageScanner.findPackage(file).orElse("");

            String className = packageName.isEmpty()
                    ? simpleName
                    : packageName + "." + simpleName;

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
