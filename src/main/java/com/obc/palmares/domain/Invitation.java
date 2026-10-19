package com.obc.palmares.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "invitation")
public class Invitation {

    @Id
    @Column(length = 64)
    private String token;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(name = "personne_id")
    private Long personneId;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Column(name = "consomme_le")
    private Instant consommeLe;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPersonneId() {
        return personneId;
    }

    public void setPersonneId(Long personneId) {
        this.personneId = personneId;
    }

    public Instant getExpireLe() {
        return expireLe;
    }

    public void setExpireLe(Instant expireLe) {
        this.expireLe = expireLe;
    }

    public Instant getConsommeLe() {
        return consommeLe;
    }

    public void setConsommeLe(Instant consommeLe) {
        this.consommeLe = consommeLe;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    @Column(name = "cree_le", nullable = false, updatable = false)
    private Instant creeLe = Instant.now();

    public boolean estValide() {
        return consommeLe == null && expireLe.isAfter(Instant.now());
    }

    // getters/setters
}