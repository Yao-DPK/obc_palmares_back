package com.obc.palmares.domain.similarite;

public final class JaroWinkler {

    private static final double PREFIX_SCALE = 0.1;
    private static final int MAX_PREFIX = 4;

    private JaroWinkler() {}

    public static double similarite(String a, String b) {
        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        int fenetre = Math.max(a.length(), b.length()) / 2 - 1;
        if (fenetre < 0) fenetre = 0;

        boolean[] aMatch = new boolean[a.length()];
        boolean[] bMatch = new boolean[b.length()];
        int matches = 0;

        for (int i = 0; i < a.length(); i++) {
            int debut = Math.max(0, i - fenetre);
            int fin = Math.min(b.length() - 1, i + fenetre);
            for (int j = debut; j <= fin; j++) {
                if (bMatch[j] || a.charAt(i) != b.charAt(j)) continue;
                aMatch[i] = true;
                bMatch[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;

        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < a.length(); i++) {
            if (!aMatch[i]) continue;
            while (!bMatch[k]) k++;
            if (a.charAt(i) != b.charAt(k)) transpositions++;
            k++;
        }
        transpositions /= 2;

        double jaro = (matches / (double) a.length()
                     + matches / (double) b.length()
                     + (matches - transpositions) / (double) matches) / 3.0;

        int prefixe = 0;
        int max = Math.min(MAX_PREFIX, Math.min(a.length(), b.length()));
        while (prefixe < max && a.charAt(prefixe) == b.charAt(prefixe)) prefixe++;

        return jaro + prefixe * PREFIX_SCALE * (1.0 - jaro);
    }
}