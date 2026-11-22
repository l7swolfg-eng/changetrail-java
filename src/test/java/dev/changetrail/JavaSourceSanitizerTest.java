package dev.changetrail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaSourceSanitizerTest {

    @Test
    void removesCommentsAndStringLiterals() {
        String source = """
                class AuditLog {
                    // PaymentService is mentioned here.
                    /* PaymentService is also mentioned here. */
                    String message = "PaymentService";
                }
                """;

        String cleaned = JavaSourceSanitizer.stripNonCode(source);

        assertFalse(cleaned.contains("PaymentService"));
        assertTrue(cleaned.contains("class AuditLog"));
    }

    @Test
    void preservesActualClassReferences() {
        String source = """
                class OrderService {
                    private PaymentService payment;
                    // PaymentService is also in this comment.
                }
                """;

        String cleaned = JavaSourceSanitizer.stripNonCode(source);

        assertTrue(cleaned.contains("PaymentService payment"));
        assertFalse(cleaned.contains("this comment"));
    }

    @Test
    void handlesEscapedQuotesAndCharacters() {
        String source =
                "String message = \"He said \\\"PaymentService\\\"\";\n" +
                        "char letter = 'P';\n" +
                        "OrderService order;\n";

        String cleaned = JavaSourceSanitizer.stripNonCode(source);

        assertFalse(cleaned.contains("PaymentService"));
        assertFalse(cleaned.contains("'P'"));
        assertTrue(cleaned.contains("OrderService order;"));
    }

    @Test
    void handlesTextBlocks() {
        String source =
                "String description = \"\"\"\n" +
                        "PaymentService\n" +
                        "\"\"\";\n" +
                        "OrderService order;\n";

        String cleaned = JavaSourceSanitizer.stripNonCode(source);

        assertFalse(cleaned.contains("PaymentService"));
        assertTrue(cleaned.contains("OrderService order;"));

        // Sanitizing should preserve the source length.
        assertEquals(source.length(), cleaned.length());
    }
}
