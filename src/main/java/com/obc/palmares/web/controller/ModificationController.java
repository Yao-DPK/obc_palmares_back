package com.obc.palmares.web.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.obc.palmares.web.dto.PersonneRequest;
import com.obc.palmares.web.service.ModificationService;

import jakarta.validation.Valid;

@RestController 
public class ModificationController{
    
    private final ModificationService service;

    public ModificationController(ModificationService service){
        this.service = service;
        
    }

    @PostMapping("/api/personnes/{id}/modification-demande")
    public ResponseEntity<?> demander(@PathVariable Long id,
                                       @Valid @RequestBody PersonneRequest req,
                                       Authentication auth) {
        service.demanderModification(id, req);
        return ResponseEntity.ok(Map.of("emailEnvoye", true));
    }

    @PostMapping("/api/confirmer-modification/{token}")
    public ResponseEntity<?> confirmer(@PathVariable String token) throws IOException {
        service.appliquerModification(token);
        return ResponseEntity.ok(Map.of("applique", true));
    }
}