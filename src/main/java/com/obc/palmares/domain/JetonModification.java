package com.obc.palmares.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jeton_modification")
public class JetonModification {

    @Id
    @Column(length = 64)
    private String token;

    @Column(name = "personne_id", nullable = false)
    private Long personneId;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Column(name = "utilise_le")
    private Instant utiliseLe;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private Instant creeLe = Instant.now();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getPersonneId() {
        return personneId;
    }

    public void setPersonneId(Long personneId) {
        this.personneId = personneId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getExpireLe() {
        return expireLe;
    }

    public void setExpireLe(Instant expireLe) {
        this.expireLe = expireLe;
    }

    public Instant getUtiliseLe() {
        return utiliseLe;
    }

    public void setUtiliseLe(Instant utiliseLe) {
        this.utiliseLe = utiliseLe;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    // getters/setters
}