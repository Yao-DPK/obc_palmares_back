package com.obc.palmares.web.controller;

import com.obc.palmares.domain.Invitation;
import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.RoleCode;
import com.obc.palmares.web.dto.*;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/soumissions")
public class SoumissionController {

    private final InvitationService invitations;
    private final PersonneService personnes;
    private final AffectationService affectations;

    public SoumissionController(InvitationService invitations,
                                PersonneService personnes,
                                AffectationService affectations) {
        this.invitations = invitations;
        this.personnes = personnes;
        this.affectations = affectations;
    }

    @PostMapping
    public ResponseEntity<?> creer(@Valid @RequestBody SoumissionRequest req) {
        Invitation inv;
        boolean nouvelleInvitation = false;

        if (req.token() == null || req.token().isBlank()) {
            inv = invitations.creerAnonyme();
            nouvelleInvitation = true;
        } else {
            inv = invitations.valider(req.token());
            if (inv.getPersonneId() != null) {
                throw new BusinessException("Invitation déjà utilisée");
            }
        }

        verifierRoleAutorise(req.roleCode());

        var resultat = personnes.creerAvecDetection(req.personne(), false);
        if (resultat.statut() == ResultatCreation.Statut.DOUBLON_SUSPECT) {
            if (nouvelleInvitation) invitations.supprimer(inv.getToken());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResultatCreationResponse.from(resultat));
        }

        Personne p = resultat.personne();

        affectations.creer(p.getId(), new AffectationRequest(
            req.roleCode(), req.anneeDebut(), req.anneeFin(), null, null));

        invitations.lierPersonne(inv.getToken(), p.getId());

        return ResponseEntity.created(URI.create("/api/personnes/" + p.getId()))
            .body(Map.of(
                "personneId", p.getId(),
                "token", inv.getToken()
            ));
    }

    @PostMapping("/affectations")
    public ResponseEntity<?> ajouterAffectation(@Valid @RequestBody AffectationSoumissionRequest req) {
        var inv = invitations.valider(req.token());
        if (inv.getPersonneId() == null) {
            throw new BusinessException("Aucune personne liée à cette invitation");
        }
        verifierRoleAutorise(req.roleCode());

        var a = affectations.creer(inv.getPersonneId(), new AffectationRequest(
            req.roleCode(), req.anneeDebut(), req.anneeFin(), null, null));
        return ResponseEntity.ok(Map.of("affectationId", a.getId()));
    }

    private void verifierRoleAutorise(RoleCode code) {
        if (code != RoleCode.FORME && code != RoleCode.JOUEUR && code != RoleCode.ENCADREUR) {
            throw new BusinessException("Statut non autorisé en self-service");
        }
    }
}