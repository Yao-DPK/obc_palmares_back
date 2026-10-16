package com.obc.palmares.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonneRepository
        extends JpaRepository<Personne, Long>, PersonneRepositoryCustom {

    Page<Personne> findAllByVisibiliteNot(Personne.Visibilite visibilite, Pageable pageable);

    @Query(value = """
        SELECT * FROM personne
        WHERE nom_normalise % :q
        ORDER BY similarity(nom_normalise, :q) DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Personne> rechercherSimilaire(@Param("q") String q);
}