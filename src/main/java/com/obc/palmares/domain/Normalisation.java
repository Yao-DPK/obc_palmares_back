package com.obc.palmares.domain;

import java.text.Normalizer;
import java.util.Locale;

public final class Normalisation {

    private Normalisation() {}

    /** "Kouassi" -> "kouassi", "Yao-Koné" -> "yao kone" */
    public static String nom(String prenom, String nom) {
        String s = (prenom + " " + nom)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[-']", " ");
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return s.replaceAll("\\s+", " ").trim();
    }
}