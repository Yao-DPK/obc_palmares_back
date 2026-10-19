package com.obc.palmares.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonneSnapshotRepository extends JpaRepository<PersonneSnapshot, Long>{
    List<PersonneSnapshot> findByPersonneIdOrderByCreerLeDesc(Long personneId);
}