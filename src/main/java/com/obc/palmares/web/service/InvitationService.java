package com.obc.palmares.web.service;

import com.obc.palmares.domain.Invitation;
import com.obc.palmares.domain.InvitationRepository;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@Transactional
public class InvitationService {

    private static final SecureRandom RNG = new SecureRandom();

    private final InvitationRepository repo;

    public InvitationService(InvitationRepository repo) {
        this.repo = repo;
    }

    public Invitation creer(String email) {
        Invitation inv = new Invitation();
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        inv.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        inv.setEmail(email);
        inv.setExpireLe(Instant.now().plus(30, ChronoUnit.DAYS));
        return repo.save(inv);
    }

    @Transactional(readOnly = true)
    public Invitation trouver(String token) {
        return repo.findById(token)
            .orElseThrow(() -> new NotFoundException("Invitation inconnue"));
    }

    public Invitation valider(String token) {
        Invitation inv = trouver(token);
        if (inv.getConsommeLe() != null) {
            throw new BusinessException("Cette invitation a déjà été utilisée");
        }
        if (inv.getExpireLe().isBefore(Instant.now())) {
            throw new BusinessException("Cette invitation a expiré");
        }
        return inv;
    }

    public Invitation lierPersonne(String token, Long personneId) {
        Invitation inv = valider(token);
        if (inv.getPersonneId() != null) {
            throw new BusinessException("Invitation déjà liée à une personne");
        }
        inv.setPersonneId(personneId);
        return repo.save(inv);
    }

    public void consommer(String token) {
        Invitation inv = valider(token);
        inv.setConsommeLe(Instant.now());
        repo.save(inv);
    }
}