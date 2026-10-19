package com.obc.palmares.web.service;

import com.obc.palmares.domain.Normalisation;
import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneRepository;
import com.obc.palmares.domain.similarite.JaroWinkler;
import com.obc.palmares.domain.similarite.Levenshtein;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class SimilariteService {

    public static final double SEUIL_DOUBLON  = 0.85;
    public static final double SEUIL_CANDIDAT = 0.60;

    /** Relevé depuis la valeur par défaut (0.3) qui rend l'index GIN non sélectif. */
    private static final double TRGM_SEUIL = 0.45;
    private static final int    LIMITE_CANDIDATS = 20;

    private final PersonneRepository repo;

    public SimilariteService(PersonneRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<Candidat> chercherCandidats(String prenom, String nom, LocalDate dateNaissance) {
        String requete = Normalisation.nom(prenom, nom);

        List<Personne> bruts = repo.rechercherSimilaireIndexe(requete, TRGM_SEUIL, LIMITE_CANDIDATS);
        if (bruts.isEmpty()) return List.of();

        return bruts.stream()
            .map(p -> new Candidat(p, scorer(p, prenom, nom, dateNaissance)))
            .filter(c -> c.score() >= SEUIL_CANDIDAT)
            .sorted(Comparator.comparingDouble(Candidat::score).reversed())
            .limit(5)
            .toList();
    }

    public double scorer(Personne existante, String prenom, String nom, LocalDate dateNaissance) {
        String a = existante.getNomNormalise();
        String b = Normalisation.nom(prenom, nom);

        double sNom = Math.max(Levenshtein.ratio(a, b), JaroWinkler.similarite(a, b));

        double sDate = 0.5;
        if (dateNaissance != null && existante.getDateNaissance() != null) {
            sDate = dateNaissance.equals(existante.getDateNaissance()) ? 1.0 : 0.0;
        }

        return 0.9 * sNom + 0.1 * sDate;
    }

    public boolean estDoublonSur(Candidat c) {
        return c.score() >= SEUIL_DOUBLON;
    }
}