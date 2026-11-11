package com.zuehlke.securesoftwaredevelopment.repository;

/**
 * Small helpers for building safe SQL parameters.
 */
final class SqlUtils {

    /** Escape character used together with {@code LIKE ... ESCAPE '\'}. */
    static final char LIKE_ESCAPE = '\\';

    private SqlUtils() {
    }

    /**
     * Builds an upper-cased "contains" pattern for a {@code LIKE} predicate.
     * <p>
     * The value is still passed as a bind parameter (so it can never change the query structure),
     * but the {@code %} and {@code _} wildcards are escaped as well, so a user cannot turn
     * a search for {@code "_"} into "match everything".
     */
    static String containsPattern(String term) {
        String value = term == null ? "" : term.trim().toUpperCase();
        StringBuilder escaped = new StringBuilder(value.length() + 2).append('%');
        for (char c : value.toCharArray()) {
            if (c == '%' || c == '_' || c == LIKE_ESCAPE) {
                escaped.append(LIKE_ESCAPE);
            }
            escaped.append(c);
        }
        return escaped.append('%').toString();
    }
}
