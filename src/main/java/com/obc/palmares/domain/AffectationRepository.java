package com.obc.palmares.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findAllByPersonneIdOrderBySaisonDesc(Long personneId);
    boolean existsByPersonneIdAndRoleCodeAndSaison(Long personneId, String roleCode, String saison);
}