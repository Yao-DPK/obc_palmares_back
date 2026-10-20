package com.obc.palmares.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findAllByPersonneIdOrderBySaisonDesc(Long personneId);
boolean existsByPersonneIdAndRoleCodeAndAnneeDebut(Long personneId, String roleCode, Integer anneeDebut);
}