package com.obc.palmares.web.dto;

import com.obc.palmares.web.service.Candidat;
import com.obc.palmares.web.service.ResultatCreation;

import java.util.List;

public record ResultatCreationResponse(
    String statut,
    PersonneResponse personne,
    List<CandidatResponse> candidats
) {
    public record CandidatResponse(PersonneResponse personne, double score, int scorePourcent) {}

    public static ResultatCreationResponse from(ResultatCreation r) {
        return new ResultatCreationResponse(
            r.statut().name(),
            r.personne() == null ? null : PersonneResponse.from(r.personne()),
            r.candidats().stream()
                .map(c -> new CandidatResponse(
                    PersonneResponse.from(c.personne()),
                    c.score(),
                    (int) Math.round(c.score() * 100)))
                .toList()
        );
    }
}