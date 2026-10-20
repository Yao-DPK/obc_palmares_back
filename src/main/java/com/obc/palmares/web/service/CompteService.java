package com.obc.palmares.web.service;

import com.obc.palmares.domain.*;
import com.obc.palmares.web.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class CompteService {

    private final CompteRepository repo;
    private final PasswordEncoder encoder;

    public CompteService(CompteRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Compte creer(Long personneId, String username, String email, String password) {
        if (repo.existsByUsername(username)) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris");
        }
        if (repo.findByPersonneId(personneId).isPresent()) {
            throw new BusinessException("Un compte existe déjà pour cette personne");
        }
        Compte c = new Compte();
        c.setPersonneId(personneId);
        c.setUsername(username);
        c.setEmail(email);
        c.setPasswordHash(encoder.encode(password));
        return repo.save(c);
    }

    public Compte authentifier(String username, String password) {
        Compte c = repo.findByUsername(username)
            .orElseThrow(() -> new BusinessException("Identifiants invalides"));
        if (!encoder.matches(password, c.getPasswordHash())) {
            throw new BusinessException("Identifiants invalides");
        }
        c.setDerniereConnexion(Instant.now());
        return repo.save(c);
    }
}