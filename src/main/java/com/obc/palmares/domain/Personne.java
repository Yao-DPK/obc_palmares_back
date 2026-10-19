package com.obc.palmares.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "personne")
public class Personne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "nom_normalise", nullable = false, length = 200)
    private String nomNormalise;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "text")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibilite visibilite = Visibilite.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut = Statut.ACTIF;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private Instant creeLe = Instant.now();

    public Instant getConsentementRgpdLe() {
        return consentementRgpdLe;
    }

    public void setConsentementRgpdLe(Instant consentementRgpdLe) {
        this.consentementRgpdLe = consentementRgpdLe;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Column(name = "maj_le", nullable = false)
    private Instant majLe = Instant.now();

    @Column(name = "consentement_rgpd_le")
    private Instant consentementRgpdLe;

    @Column(nullable = false, length = 30)
    private String source = "SELF_SERVICE";

    // getters/setters

    @PreUpdate
    void onUpdate() { this.majLe = Instant.now(); }

    // getters/setters à générer (ou Lombok si tu préfères)
    
    public enum Visibilite { PUBLIC, PRIVE, MASQUE }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomNormalise() {
        return nomNormalise;
    }

    public void setNomNormalise(String nomNormalise) {
        this.nomNormalise = nomNormalise;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Visibilite getVisibilite() {
        return visibilite;
    }

    public void setVisibilite(Visibilite visibilite) {
        this.visibilite = visibilite;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public Instant getMajLe() {
        return majLe;
    }

    public void setMajLe(Instant majLe) {
        this.majLe = majLe;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    
}