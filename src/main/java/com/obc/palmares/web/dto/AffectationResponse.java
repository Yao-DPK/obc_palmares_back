package com.obc.palmares.web.dto;

import com.obc.palmares.domain.Affectation;

import java.time.Instant;

public record AffectationResponse(
    Long id,
    Long personneId,
    String roleCode,
    String saison,
    String equipe,
    String commentaire,
    Instant creeLe
) {
    public static AffectationResponse from(Affectation a) {
        return new AffectationResponse(
            a.getId(), a.getPersonne().getId(),
            a.getRoleCode(), a.getSaison(),
            a.getEquipe(), a.getCommentaire(),
            a.getCreeLe()
        );
    }
}