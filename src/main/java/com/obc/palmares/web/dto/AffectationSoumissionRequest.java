package com.obc.palmares.web.dto;

import com.obc.palmares.domain.RoleCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AffectationSoumissionRequest(
    @NotBlank String token,
    @NotNull RoleCode roleCode,
    @NotNull Integer anneeDebut,
    Integer anneeFin
) {}