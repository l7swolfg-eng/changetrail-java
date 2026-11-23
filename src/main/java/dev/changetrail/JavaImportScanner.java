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

                if (importName.startsWith("static ")) {
                    // A static import names a member of a class, or *.
                    // Keep the owning class so the graph can resolve it.
                    String member = importName.substring(7).trim();
                    int lastDot = member.lastIndexOf('.');

                    if (lastDot > 0) {
                        imports.add(member.substring(0, lastDot));
                    }
                } else {
                    imports.add(importName);
                }
            }
        }

        return imports;
    }
}
