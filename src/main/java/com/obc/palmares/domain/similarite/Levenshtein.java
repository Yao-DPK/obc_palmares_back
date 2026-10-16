package com.obc.palmares.domain.similarite;

public final class Levenshtein {

    private Levenshtein() {}

    /** Distance d'édition entre deux chaînes, en O(min(a,b)) mémoire. */
    public static int distance(String a, String b) {
        if (a.equals(b)) return 0;
        if (a.isEmpty()) return b.length();
        if (b.isEmpty()) return a.length();

        // On garde la chaîne la plus courte en ligne
        if (a.length() > b.length()) { String t = a; a = b; b = t; }

        int[] prec = new int[a.length() + 1];
        int[] cour = new int[a.length() + 1];
        for (int i = 0; i <= a.length(); i++) prec[i] = i;

        for (int j = 1; j <= b.length(); j++) {
            cour[0] = j;
            for (int i = 1; i <= a.length(); i++) {
                int cout = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                cour[i] = Math.min(Math.min(cour[i - 1] + 1, prec[i] + 1), prec[i - 1] + cout);
            }
            int[] t = prec; prec = cour; cour = t;
        }
        return prec[a.length()];
    }

    /** Similarité normalisée entre 0 et 1. */
    public static double ratio(String a, String b) {
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        return 1.0 - ((double) distance(a, b) / max);
    }
}