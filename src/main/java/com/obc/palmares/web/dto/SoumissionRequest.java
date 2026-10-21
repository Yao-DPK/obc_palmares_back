package com.obc.palmares.web.dto;

import com.obc.palmares.domain.RoleCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SoumissionRequest (
    String token,
    @NotNull @Valid PersonneRequest personne,
    @NotNull RoleCode roleCode,
    @NotNull Integer anneeDebut,
    Integer anneeFin
) {}
