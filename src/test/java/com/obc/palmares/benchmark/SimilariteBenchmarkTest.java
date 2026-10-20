package com.obc.palmares.benchmark;

import com.obc.palmares.domain.Normalisation;
import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneRepository;
import com.obc.palmares.domain.similarite.JaroWinkler;
import com.obc.palmares.domain.similarite.Levenshtein;
import com.obc.palmares.web.service.SimilariteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Testcontainers
@SpringBootTest
class SimilariteBenchmarkTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer pg = new PostgreSQLContainer("postgres:16-alpine")
        .withInitScript("db/init-testcontainers.sql");

    @Autowired PersonneRepository repo;
    @Autowired SimilariteService similariteService;

    private static final String[] PRENOMS = {
        "Yao","Kouassi","Awa","Moussa","Fatou","Ibrahima","Aminata","Koffi","Aya","Sekou",
        "Mariam","Ousmane","Nadia","Bakary","Cheikh","Rokhaya","Lamine","Soda","Mamadou","Ali"
    };

    private static final String[] NOMS = {
        "Konan","Diallo","Traore","Kouassi","Ndiaye","Diop","Toure","Coulibaly","Bamba","Sanogo",
        "Sylla","Camara","Fofana","Kone","Ouattara","Yao","Gbagbo","Zadi","Doumbia","Cisse"
    };

    @BeforeEach
    void clean() { repo.deleteAll(); }

    @Test
    void benchmark_naif_vs_indexe() {
        for (int taille : new int[]{500, 5_000, 20_000, 50_000}) {
            repo.deleteAll();
            seed(taille);
            mesurer(taille, 200);
        }
    }

    @Test
    void benchmark_sequentiel_vs_parallele() {
        Random r = new Random(1);
        String[] noms = new String[2_000];
        for (int i = 0; i < noms.length; i++) {
            noms[i] = Normalisation.nom(
                PRENOMS[r.nextInt(PRENOMS.length)],
                NOMS[r.nextInt(NOMS.length)]);
        }
        String cible = Normalisation.nom("Yao", "Konan");

        long t0 = System.nanoTime();
        long s1 = 0;
        for (String n : noms) if (score(n, cible) >= 0.60) s1++;
        long tSeq = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        long s2 = Arrays.stream(noms).parallel()
            .filter(n -> score(n, cible) >= 0.60)
            .count();
        long tPar = System.nanoTime() - t1;

        System.out.printf("%n=== Séquentiel vs parallèle, %d candidats ===%n", noms.length);
        System.out.printf("Séquentiel : %6.2f ms  [%d matchs]%n", tSeq / 1e6, s1);
        System.out.printf("Parallèle  : %6.2f ms  [%d matchs]%n", tPar / 1e6, s2);
        System.out.printf("Gain       : x%.2f%n", (double) tSeq / tPar);
    }

    private void seed(int n) {
        Random r = new Random(42);
        List<Personne> batch = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String prenom = PRENOMS[r.nextInt(PRENOMS.length)];
            String nom = NOMS[r.nextInt(NOMS.length)] + (i / 400 == 0 ? "" : String.valueOf(i / 400));

            Personne p = new Personne();
            p.setPrenom(prenom);
            p.setNom(nom);
            p.setNomNormalise(Normalisation.nom(prenom, nom));
            p.setDateNaissance(LocalDate.of(
                1950 + r.nextInt(60), 1 + r.nextInt(12), 1 + r.nextInt(28)));
            batch.add(p);
        }
        repo.saveAll(batch);
    }

    private void mesurer(int taille, int iterations) {
        Random r = new Random(7);
        String[][] cibles = new String[iterations][];
        for (int i = 0; i < iterations; i++) {
            cibles[i] = new String[]{
                PRENOMS[r.nextInt(PRENOMS.length)],
                NOMS[r.nextInt(NOMS.length)]
            };
        }

        Runtime rt = Runtime.getRuntime();
        System.gc();
        long memAvant = rt.totalMemory() - rt.freeMemory();

        List<Personne> toutes = repo.findAll();
        String[] normes = toutes.stream().map(Personne::getNomNormalise).toArray(String[]::new);

        System.gc();
        long memApres = rt.totalMemory() - rt.freeMemory();
        long memNaif = memApres - memAvant;

        long t0 = System.nanoTime();
        int totalNaif = 0;
        for (String[] c : cibles) {
            String req = Normalisation.nom(c[0], c[1]);
            for (String n : normes) {
                if (score(n, req) >= 0.60) totalNaif++;
            }
        }
        long tNaif = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        int totalIndexe = 0;
        for (String[] c : cibles) {
            totalIndexe += similariteService.chercherCandidats(c[0], c[1], null).size();
        }
        long tIndexe = System.nanoTime() - t1;

        double rapport = (double) tNaif / tIndexe;

        System.out.printf("%n=== %d fiches, %d requêtes ===%n", taille, iterations);
        System.out.printf("Naïf   : %8.1f ms  (%6.3f ms/req)  [matches=%d]  [mem=%.1f Mo]%n",
            tNaif / 1e6, tNaif / 1e6 / iterations, totalNaif, memNaif / 1e6);
        System.out.printf("Indexé : %8.1f ms  (%6.3f ms/req)  [matches=%d]  [mem=O(1)]%n",
            tIndexe / 1e6, tIndexe / 1e6 / iterations, totalIndexe);
        if (rapport >= 1.0) {
            System.out.printf("→ Indexé gagne : x%.2f%n", rapport);
        } else {
            System.out.printf("→ Naïf gagne   : x%.2f%n", 1.0 / rapport);
        }
    }

    private double score(String a, String b) {
        double s = Math.max(Levenshtein.ratio(a, b), JaroWinkler.similarite(a, b));
        return 0.9 * s + 0.05;
    }
}