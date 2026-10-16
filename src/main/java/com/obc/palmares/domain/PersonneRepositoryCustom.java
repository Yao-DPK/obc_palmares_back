package com.obc.palmares.domain;

import java.util.List;

public interface PersonneRepositoryCustom {

    /**
     * Recherche floue via l'index GIN trigram de PostgreSQL.
     * Le scoring fin (Levenshtein / Jaro-Winkler) est fait côté Java.
     *
     * @param requete chaîne normalisée (prénom + nom)
     * @param seuil   seuil pg_trgm.similarity_threshold (0.3 par défaut, trop bas)
     * @param limite  nombre max de candidats bruts retournés
     */
    List<Personne> rechercherSimilaireIndexe(String requete, double seuil, int limite);
}