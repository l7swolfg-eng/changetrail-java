package dev.changetrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaImportScanner {

    public static List<String> findImports(Path javaFile) throws IOException {
        List<String> imports = new ArrayList<>();

        // Remove comments and literals before looking for imports.
        String source = Files.readString(javaFile);
        String cleaned = JavaSourceSanitizer.stripNonCode(source);

        for (String line : cleaned.lines().toList()) {
            String trimmed = line.trim();

            if (trimmed.startsWith("import ") && trimmed.endsWith(";")) {
                String importName = trimmed.substring(
                        7, trimmed.length() - 1
                ).trim();

                if (!importName.startsWith("static ")) {
                    imports.add(importName);
                }
            }
        }

        return imports;
    }
}
