package com.obc.palmares.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "compte")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "personne_id", nullable = false, unique = true)
    private Long personneId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonneId() {
        return personneId;
    }

    public void setPersonneId(Long personneId) {
        this.personneId = personneId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getEmailConfirmeLe() {
        return emailConfirmeLe;
    }

    public void setEmailConfirmeLe(Instant emailConfirmeLe) {
        this.emailConfirmeLe = emailConfirmeLe;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public Instant getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(Instant derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(name = "email_confirme_le")
    private Instant emailConfirmeLe;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private Instant creeLe = Instant.now();

    @Column(name = "derniere_connexion")
    private Instant derniereConnexion;

    // getters/setters
}