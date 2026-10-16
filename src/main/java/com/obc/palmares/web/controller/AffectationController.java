package com.obc.palmares.web.controller;

import com.obc.palmares.web.service.AffectationService;
import com.obc.palmares.web.dto.AffectationRequest;
import com.obc.palmares.web.dto.AffectationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AffectationController {

    private final AffectationService service;

    public AffectationController(AffectationService service) {
        this.service = service;
    }

    @GetMapping("/personnes/{personneId}/affectations")
    public List<AffectationResponse> lister(@PathVariable Long personneId) {
        return service.listerParPersonne(personneId).stream()
            .map(AffectationResponse::from).toList();
    }

    @PostMapping("/personnes/{personneId}/affectations")
    public ResponseEntity<AffectationResponse> creer(
            @PathVariable Long personneId,
            @Valid @RequestBody AffectationRequest req,
            UriComponentsBuilder uriBuilder) {
        var a = service.creer(personneId, req);
        URI location = uriBuilder.path("/api/affectations/{id}").buildAndExpand(a.getId()).toUri();
        return ResponseEntity.created(location).body(AffectationResponse.from(a));
    }

    @PutMapping("/affectations/{id}")
    public AffectationResponse modifier(@PathVariable Long id,
                                        @Valid @RequestBody AffectationRequest req) {
        return AffectationResponse.from(service.modifier(id, req));
    }

    @DeleteMapping("/affectations/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}