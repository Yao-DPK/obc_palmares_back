package com.obc.palmares.web.dto;

import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.Statut;

import java.time.Instant;
import java.time.LocalDate;

public record PersonneResponse(
    Long id,
    String nom,
    String prenom,
    LocalDate dateNaissance,
    String photoUrl,
    String bio,
    Personne.Visibilite visibilite,
    Statut statut,
    Instant creeLe,
    Instant majLe
) {
    public static PersonneResponse from(Personne p) {
        return new PersonneResponse(
            p.getId(), p.getNom(), p.getPrenom(),
            p.getDateNaissance(),
            p.getPhotoUrl(), p.getBio(),
            p.getVisibilite(), p.getStatut(),
            p.getCreeLe(), p.getMajLe()
        );
    }
}