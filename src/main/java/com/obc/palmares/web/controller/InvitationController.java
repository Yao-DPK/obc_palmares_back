package com.obc.palmares.web.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obc.palmares.web.service.InvitationService;

@RestController 
@RequestMapping("/api/invitation")
public class InvitationController {
    
    private final InvitationService service;
    
    public InvitationController(InvitationService service){
        this.service = service;
    }

    @GetMapping("/{token}")
    public Map<String, Object> etat(@PathVariable String token) {
        var inv = service.trouver(token);
        String etat;
        if (inv.getConsommeLe() != null) etat = "CONSOMME";
        else if (inv.getExpireLe().isBefore(java.time.Instant.now())) etat = "EXPIRE";
        else if (inv.getPersonneId() == null) etat = "NOUVEAU";
        else etat = "EN_COURS";
        return Map.of(
            "etat", etat,
            "personneId", inv.getPersonneId() == null ? "" : inv.getPersonneId()
        );
    }
}
