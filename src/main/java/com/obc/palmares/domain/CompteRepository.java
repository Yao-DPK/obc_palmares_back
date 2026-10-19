package com.obc.palmares.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    Optional<Compte> findByUsername(String username);
    Optional<Compte> findByPersonneId(Long personneId);
    boolean existsByUsername(String username);
}