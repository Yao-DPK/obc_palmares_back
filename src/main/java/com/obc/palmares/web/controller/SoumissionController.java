package com.obc.palmares.web.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneRepository;
import com.obc.palmares.web.dto.AffectationRequest;
import com.obc.palmares.web.dto.AffectationSoumissionRequest;
import com.obc.palmares.web.dto.PersonneRequest;
import com.obc.palmares.web.dto.ResultatCreationResponse;
import com.obc.palmares.web.dto.SoumissionRequest;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.service.AffectationService;
import com.obc.palmares.web.service.InvitationService;
import com.obc.palmares.web.service.PersonneService;
import com.obc.palmares.web.service.ResultatCreation;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.Instant;
import com.obc.palmares.domain.RoleCode;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/soumissions")
public class SoumissionController {
    
    private final InvitationService invitations;
    private final PersonneService personnes;
    private final AffectationService affectations;



    public SoumissionController(InvitationService invitations, PersonneService personnes, AffectationService affectations){
        this.personnes = personnes;
        this.invitations = invitations;
        this.affectations = affectations;
    }

    private void verifierRoleAutorise(RoleCode code) {
        if (code != RoleCode.FORME && code != RoleCode.JOUEUR) {
            throw new BusinessException("Statut non autorisé en self-service");
        }
    }


    @PostMapping
    public ResponseEntity<?> creer(@Valid @RequestBody SoumissionRequest req) {
        var inv = invitations.valider(req.token());
        if (inv.getPersonneId() != null) {
            throw new BusinessException("Invitation déjà utilisée pour une première soumission");
        }

        verifierRoleAutorise(req.roleCode());

        PersonneRequest personne = req.personne();
        var resultat = personnes.creerAvecDetection(personne, false);
        if (resultat.statut() == ResultatCreation.Statut.DOUBLON_SUSPECT) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResultatCreationResponse.from(resultat));
        }

        Personne p = resultat.personne();
        p.setConsentementRgpdLe(java.time.Instant.now());
        personnes.enregistrerConsentement(p);

        AffectationRequest aff = new AffectationRequest(
            req.roleCode(), req.anneeDebut(), req.anneeFin(), null, null);
        affectations.creer(p.getId(), aff);

        invitations.lierPersonne(req.token(), p.getId());

        return ResponseEntity.created(java.net.URI.create("/api/personnes/" + p.getId()))
            .body(Map.of("personneId", p.getId()));
    }

    @PostMapping("/affectations")
    public ResponseEntity<?> ajouterAffectation(@Valid @RequestBody AffectationSoumissionRequest req) {
        var inv = invitations.valider(req.token());
        if (inv.getPersonneId() == null) {
            throw new BusinessException("Aucune personne liée à cette invitation");
        }

        verifierRoleAutorise(req.roleCode());

        AffectationRequest aff = new AffectationRequest(
            req.roleCode(), req.anneeDebut(), req.anneeFin(), null, null);
        var a = affectations.creer(inv.getPersonneId(), aff);
        return ResponseEntity.ok(Map.of("affectationId", a.getId()));
    }
}
