package ar.edu.itba.paw.models.util;

import java.util.Locale;

/** Canonical email form for storage and lookup: trim and lowercase with {@link Locale***REMOVED***ROOT}. */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
