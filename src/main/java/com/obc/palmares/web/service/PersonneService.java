package com.obc.palmares.web.service;

import com.obc.palmares.domain.Normalisation;
import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneRepository;
import com.obc.palmares.web.dto.PersonneRequest;
import com.obc.palmares.web.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonneService {

    private final PersonneRepository repo;
    private final SimilariteService similariteService;

    public PersonneService(PersonneRepository repo, SimilariteService similariteService) {
        this.repo = repo;
        this.similariteService = similariteService;
    }
    
    /**
     * Crée une personne, sauf si des doublons probables sont détectés.
     * @param forcer si true, crée quand même malgré des candidats suspects.
     */
    public ResultatCreation creerAvecDetection(PersonneRequest req, boolean forcer) {
        var candidats = similariteService.chercherCandidats(
            req.prenom(), req.nom(), req.dateNaissance());

        boolean doublonCertain = candidats.stream().anyMatch(similariteService::estDoublonSur);

        if (!forcer && (doublonCertain || !candidats.isEmpty())) {
            return ResultatCreation.suspect(candidats);
        }
        Personne p = creer(req);
        return ResultatCreation.cree(p);
    }
        

    @Transactional(readOnly = true)
    public Page<Personne> lister(Pageable pageable) {
        return repo.findAllByVisibiliteNot(Personne.Visibilite.MASQUE, pageable);
    }

    @Transactional(readOnly = true)
    public Personne trouver(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Personne " + id + " introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Personne> rechercher(String q) {
        String requete = q == null ? "" : q.toLowerCase().trim();
        if (requete.isEmpty()) return List.of();
        return repo.rechercherSimilaire(requete);
    }

    public Personne creer(PersonneRequest req) {
        Personne p = new Personne();
        appliquer(p, req);
        p.setNomNormalise(Normalisation.nom(req.prenom(), req.nom()));
        return repo.save(p);
    }

    public Personne modifier(Long id, PersonneRequest req) {
        Personne p = trouver(id);
        appliquer(p, req);
        p.setNomNormalise(Normalisation.nom(req.prenom(), req.nom()));
        return repo.save(p);
    }

    public void supprimer(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Personne " + id + " introuvable");
        }
        repo.deleteById(id);
    }

    private void appliquer(Personne p, PersonneRequest req) {
        p.setNom(req.nom().trim());
        p.setPrenom(req.prenom().trim());
        p.setDateNaissance(req.dateNaissance());
        p.setPhotoUrl(req.photoUrl());
        p.setBio(req.bio());
        if (req.visibilite() != null) p.setVisibilite(req.visibilite());
        if (req.statut() != null) p.setStatut(req.statut());
    }
}