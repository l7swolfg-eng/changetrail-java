package dev.changetrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyGraphBuilder {

    // Builds a graph from a single source directory.
    public static DependencyGraph build(Path sourceRoot) throws IOException {
        List<Path> files = JavaFileScanner.findJavaFiles(sourceRoot);
        return buildFromFiles(files);
    }

    // Builds a graph containing both production and test files.
    public static DependencyGraph buildProject(Path projectRoot) throws IOException {
        Path mainRoot = projectRoot.resolve("src/main/java");
        Path testRoot = projectRoot.resolve("src/test/java");

        List<Path> files = new ArrayList<>(
                JavaFileScanner.findJavaFiles(mainRoot)
        );

        if (Files.isDirectory(testRoot)) {
            files.addAll(JavaFileScanner.findJavaFiles(testRoot));
        }

        return buildFromFiles(files);
    }

    private static DependencyGraph buildFromFiles(List<Path> files)
            throws IOException {

        DependencyGraph graph = new DependencyGraph();
        Map<String, Path> classFiles = new HashMap<>();

        // First pass: identify each class by its package and filename.
        for (Path file : files) {
            graph.addFile(file);

            String fileName = file.getFileName().toString();
            String simpleName = fileName.substring(0, fileName.length() - 5);

            String packageName = JavaPackageScanner.findPackage(file)
                    .orElse("");

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
