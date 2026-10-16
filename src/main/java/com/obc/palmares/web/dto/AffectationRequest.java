package com.obc.palmares.web.dto;

import com.obc.palmares.domain.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AffectationRequest(
    @NotNull RoleCode roleCode,
    @NotBlank @Pattern(regexp = "\\d{4}(-\\d{4})?", message = "Format attendu : 1999 ou 1999-2005")
    String saison,
    @Size(max = 50) String equipe,
    String commentaire
) {}