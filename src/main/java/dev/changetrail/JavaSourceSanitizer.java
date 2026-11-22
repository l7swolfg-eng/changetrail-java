package dev.changetrail;

public class JavaSourceSanitizer {

    private enum State {
        CODE,
        LINE_COMMENT,
        BLOCK_COMMENT,
        STRING,
        CHARACTER,
        TEXT_BLOCK
    }

    public static String stripNonCode(String source) {
        StringBuilder result = new StringBuilder();
        State state = State.CODE;

        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length()
                    ? source.charAt(i + 1) : '\0';

            switch (state) {
                case CODE -> {
                    if (current == '/' && next == '/') {
                        state = State.LINE_COMMENT;
                        result.append("  ");
                        i++;
                    } else if (current == '/' && next == '*') {
                        state = State.BLOCK_COMMENT;
                        result.append("  ");
                        i++;
                    } else if (source.startsWith("\"\"\"", i)) {
                        state = State.TEXT_BLOCK;
                        result.append("   ");
                        i += 2;
                    } else if (current == '"') {
                        state = State.STRING;
                        result.append(' ');
                    } else if (current == '\'') {
                        state = State.CHARACTER;
                        result.append(' ');
                    } else {
                        result.append(current);
                    }
                }

                case LINE_COMMENT -> {
                    if (current == '\n' || current == '\r') {
                        state = State.CODE;
                        result.append(current);
                    } else {
                        result.append(' ');
                    }
                }

                case BLOCK_COMMENT -> {
                    if (current == '*' && next == '/') {
                        state = State.CODE;
                        result.append("  ");
                        i++;
                    } else {
                        result.append(mask(current));
                    }
                }

                case STRING, CHARACTER -> {
                    char closing = state == State.STRING ? '"' : '\'';

                    if (current == '\\' && i + 1 < source.length()) {
                        result.append("  ");
                        i++;
                    } else if (current == closing) {
                        state = State.CODE;
                        result.append(' ');
                    } else {
                        result.append(mask(current));
                    }
                }

                case TEXT_BLOCK -> {
                    if (current == '\\' && i + 1 < source.length()) {
                        result.append("  ");
                        i++;
                    } else if (source.startsWith("\"\"\"", i)) {
                        state = State.CODE;
                        result.append("   ");
                        i += 2;
                    } else {
                        result.append(mask(current));
                    }
                }
            }
        }

        return result.toString();
    }

    private static char mask(char c) {
        return c == '\n' || c == '\r' ? c : ' ';
    }
}
