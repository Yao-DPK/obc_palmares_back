package com.obc.palmares.web.dto;

import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.Statut;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PersonneRequest(
    @NotBlank @Size(max = 100) String nom,
    @NotBlank @Size(max = 100) String prenom,
    LocalDate dateNaissance,
    @Size(max = 500) String photoUrl,
    String bio,
    Personne.Visibilite visibilite,
    Statut statut
) {}