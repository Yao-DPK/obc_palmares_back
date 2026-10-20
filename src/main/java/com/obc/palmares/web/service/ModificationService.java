package com.obc.palmares.web.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import com.obc.palmares.domain.*;
import com.obc.palmares.web.dto.PersonneRequest;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@Transactional
public class ModificationService {

    private static final SecureRandom RNG = new SecureRandom();

    private final JetonModificationRepository jetonRepo;
    private final PersonneRepository personneRepo;
    private final CompteRepository compteRepo;
    private final MailService mail;
    private final SnapshotService snapshots;
    private final JsonMapper mapper;

    public ModificationService(JetonModificationRepository jetonRepo,
                               PersonneRepository personneRepo,
                               CompteRepository compteRepo,
                               MailService mail,
                               SnapshotService snapshots,
                               JsonMapper mapper) {
        this.jetonRepo = jetonRepo;
        this.personneRepo = personneRepo;
        this.compteRepo = compteRepo;
        this.mail = mail;
        this.snapshots = snapshots;
        this.mapper = mapper;
    }

    public void demanderModification(Long personneId, PersonneRequest payload) {
        Compte c = compteRepo.findByPersonneId(personneId)
            .orElseThrow(() -> new NotFoundException("Compte introuvable"));
        Personne p = personneRepo.findById(personneId)
            .orElseThrow(() -> new NotFoundException("Personne introuvable"));

        JetonModification jeton = new JetonModification();
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        jeton.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        jeton.setPersonneId(personneId);
        try {
            jeton.setPayload(mapper.writeValueAsString(payload));
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
        jeton.setExpireLe(Instant.now().plus(30, ChronoUnit.MINUTES));
        jetonRepo.save(jeton);

        String lien = "http://localhost:5173/confirmer-modification?t=" + jeton.getToken();
        mail.envoyerConfirmationModification(c.getEmail(), p.getPrenom(), lien);
    }

    public void appliquerModification(String token) throws IOException {
        JetonModification jeton = jetonRepo.findById(token)
            .orElseThrow(() -> new NotFoundException("Jeton inconnu"));
        if (jeton.getUtiliseLe() != null) {
            throw new BusinessException("Jeton déjà utilisé");
        }
        if (jeton.getExpireLe().isBefore(Instant.now())) {
            throw new BusinessException("Jeton expiré");
        }

        Personne p = personneRepo.findById(jeton.getPersonneId())
            .orElseThrow(() -> new NotFoundException("Personne introuvable"));

        snapshots.capturerAvantModification(p, "modification:" + token);

        try {
            PersonneRequest req = mapper.readValue(jeton.getPayload(), PersonneRequest.class);
            // Appel direct aux setters — on ne passe pas par PersonneService pour éviter un double @Transactional
            if (req.nom() != null && !req.nom().isBlank()) p.setNom(req.nom().trim());
            if (req.prenom() != null && !req.prenom().isBlank()) p.setPrenom(req.prenom().trim());
            if (req.dateNaissance() != null) p.setDateNaissance(req.dateNaissance());
            if (req.photoUrl() != null) p.setPhotoUrl(req.photoUrl());
            if (req.bio() != null) p.setBio(req.bio());
            personneRepo.save(p);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }

        jeton.setUtiliseLe(Instant.now());
        jetonRepo.save(jeton);
    }
}