package com.obc.palmares.web.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obc.palmares.web.dto.CreationCompteRequest;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.service.CompteService;
import com.obc.palmares.web.service.InvitationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final InvitationService invitations;
    private final CompteService comptes;

    // constructeur
    public CompteController(InvitationService invitations, CompteService comptes){
        this.comptes = comptes;
        this.invitations = invitations;
    }

    @PostMapping
    public ResponseEntity<?> creer(@Valid @RequestBody CreationCompteRequest req) {
        var inv = invitations.valider(req.token());
        if (inv.getPersonneId() == null) {
            throw new BusinessException("Aucune personne liée à cette invitation");
        }
        var compte = comptes.creer(inv.getPersonneId(), req.username(), req.email(), req.password());
        invitations.consommer(req.token());
        return ResponseEntity.ok(Map.of("compteId", compte.getId()));
    }
}