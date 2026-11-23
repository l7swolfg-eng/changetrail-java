package dev.changetrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println(
                    "Usage: ChangeTrail <project-directory> <changed-file>"
            );
            System.exit(1);
        }

        try {
            Path projectRoot = Path.of(args[0])
                    .toAbsolutePath()
                    .normalize();

            Path changedFile = Path.of(args[1]);

            // Resolve relative file paths against the project root.
            if (!changedFile.isAbsolute()) {
                changedFile = projectRoot.resolve(changedFile);
            }

            changedFile = changedFile.toAbsolutePath().normalize();

            if (!Files.isDirectory(projectRoot)) {
                throw new IllegalArgumentException("Invalid project directory");
            }

            if (!Files.isRegularFile(changedFile)
                    || !changedFile.toString().endsWith(".java")) {
                throw new IllegalArgumentException("Invalid Java file");
            }

            Path mainRoot = projectRoot.resolve("src/main/java");
            Path testRoot = projectRoot.resolve("src/test/java");

            if (!changedFile.startsWith(mainRoot)
                    && !changedFile.startsWith(testRoot)) {
                throw new IllegalArgumentException(
                        "Changed file must be inside a Maven Java source directory"
                );
            }

            // Build the dependency graph and run BFS.
            DependencyGraph graph =
                    DependencyGraphBuilder.buildProject(projectRoot);

            List<Path> affected = graph.getAffectedFiles(changedFile)
                    .stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();

            System.out.println("\nChangeTrail - Test Impact Analysis");

            System.out.println("\nChanged file:");
            System.out.println("  " + projectRoot.relativize(changedFile));

            System.out.println("\nAffected files:");
            for (Path file : affected) {
                System.out.println("  " + projectRoot.relativize(file));
            }

            // Collect affected tests. A changed test file must run as well.
            List<Path> affectedTests = new ArrayList<>(affected.stream()
                    .filter(TestFileIdentifier::isTestFile)
                    .toList());

            if (TestFileIdentifier.isTestFile(changedFile)) {
                affectedTests.add(changedFile);
            }

            affectedTests.sort(Comparator.naturalOrder());

            System.out.println("\nAffected test files:");
            for (Path file : affectedTests) {
                System.out.println("  " + projectRoot.relativize(file));
            }

            System.out.println("\nTotal affected tests: "
                    + affectedTests.size());

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
