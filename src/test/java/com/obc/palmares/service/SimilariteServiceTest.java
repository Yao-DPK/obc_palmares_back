package com.obc.palmares.service;

import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneRepository;
import com.obc.palmares.web.service.SimilariteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class SimilariteServiceTest {

    @Container
    static PostgreSQLContainer pg = new PostgreSQLContainer("postgres:16-alpine")
    .withInitScript("db/init-testcontainers.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired PersonneRepository repo;
    @Autowired SimilariteService service;

    @BeforeEach
    void setup() {
        repo.deleteAll();
        Personne p = new Personne();
        p.setNom("Kouassi"); p.setPrenom("Yao");
        p.setNomNormalise("yao kouassi");
        p.setDateNaissance(LocalDate.of(1985, 4, 12));
        repo.save(p);
    }

    @Test
    void detecte_variantes_orthographiques() {
        var c1 = service.chercherCandidats("Yao", "Kouassi", LocalDate.of(1985, 4, 12));
        assertThat(c1).isNotEmpty();
        assertThat(c1.get(0).score()).isGreaterThan(0.95);

        var c2 = service.chercherCandidats("Yao", "Kouassi", null);
        assertThat(c2).isNotEmpty();

        var c3 = service.chercherCandidats("Yao", "Kouacy", null); // faute de frappe
        assertThat(c3).isNotEmpty();
        assertThat(c3.get(0).score()).isGreaterThan(0.80);
    }

    @Test
    void ne_detecte_pas_personne_differente() {
        var c = service.chercherCandidats("Awa", "Diallo", LocalDate.of(1990, 1, 1));
        assertThat(c).isEmpty();
    }
}