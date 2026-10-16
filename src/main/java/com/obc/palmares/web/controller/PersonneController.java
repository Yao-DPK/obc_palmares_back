package com.obc.palmares.web.controller;

import com.obc.palmares.web.dto.PersonneRequest;
import com.obc.palmares.web.dto.PersonneResponse;
import com.obc.palmares.web.dto.ResultatCreationResponse;
import com.obc.palmares.web.service.PersonneService;
import com.obc.palmares.web.service.ResultatCreation;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/personnes")
public class PersonneController {

    private final PersonneService service;

    public PersonneController(PersonneService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PersonneResponse> lister(Pageable pageable) {
        return service.lister(pageable).map(PersonneResponse::from);
    }

    @GetMapping("/{id}")
    public PersonneResponse trouver(@PathVariable Long id) {
        return PersonneResponse.from(service.trouver(id));
    }

    @GetMapping("/recherche")
    public List<PersonneResponse> rechercher(@RequestParam String q) {
        return service.rechercher(q).stream().map(PersonneResponse::from).toList();
    }

    @PostMapping("/soumettre")
    public ResponseEntity<ResultatCreationResponse> soumettre(
            @Valid @RequestBody PersonneRequest req,
            @RequestParam(defaultValue = "false") boolean forcer,
            UriComponentsBuilder uriBuilder) {

        var resultat = service.creerAvecDetection(req, forcer);
        var body = ResultatCreationResponse.from(resultat);

        if (resultat.statut() == ResultatCreation.Statut.CREE) {
            URI location = uriBuilder.path("/api/personnes/{id}")
                .buildAndExpand(resultat.personne().getId()).toUri();
            return ResponseEntity.created(location).body(body);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }

    @PostMapping
    public ResponseEntity<PersonneResponse> creer(
            @Valid @RequestBody PersonneRequest req,
            UriComponentsBuilder uriBuilder) {
        var p = service.creer(req);
        URI location = uriBuilder.path("/api/personnes/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).body(PersonneResponse.from(p));
    }

    @PutMapping("/{id}")
    public PersonneResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody PersonneRequest req) {
        return PersonneResponse.from(service.modifier(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}