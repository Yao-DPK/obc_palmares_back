package com.obc.palmares.web.service;

import com.obc.palmares.domain.Personne;

import java.util.List;

public record ResultatCreation(Statut statut, Personne personne, List<Candidat> candidats) {

    public enum Statut { CREE, DOUBLON_SUSPECT }

    public static ResultatCreation cree(Personne p) {
        return new ResultatCreation(Statut.CREE, p, List.of());
    }

    public static ResultatCreation suspect(List<Candidat> candidats) {
        return new ResultatCreation(Statut.DOUBLON_SUSPECT, null, candidats);
    }
}