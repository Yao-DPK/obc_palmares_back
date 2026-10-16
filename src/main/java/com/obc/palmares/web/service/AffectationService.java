package com.obc.palmares.web.service;

import com.obc.palmares.domain.Affectation;
import com.obc.palmares.domain.AffectationRepository;
import com.obc.palmares.domain.Personne;
import com.obc.palmares.web.dto.AffectationRequest;
import com.obc.palmares.web.exception.BusinessException;
import com.obc.palmares.web.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AffectationService {

    private final AffectationRepository repo;
    private final PersonneService personneService;

    public AffectationService(AffectationRepository repo, PersonneService personneService) {
        this.repo = repo;
        this.personneService = personneService;
    }

    @Transactional(readOnly = true)
    public List<Affectation> listerParPersonne(Long personneId) {
        personneService.trouver(personneId); // vérifie l'existence
        return repo.findAllByPersonneIdOrderBySaisonDesc(personneId);
    }

    public Affectation creer(Long personneId, AffectationRequest req) {
        Personne p = personneService.trouver(personneId);
        if (repo.existsByPersonneIdAndRoleCodeAndSaison(
                personneId, req.roleCode().name(), req.saison())) {
            throw new BusinessException(
                "Cette personne a déjà le rôle " + req.roleCode()
                + " pour la saison " + req.saison());
        }
        Affectation a = new Affectation();
        a.setPersonne(p);
        appliquer(a, req);
        return repo.save(a);
    }

    public Affectation modifier(Long affectationId, AffectationRequest req) {
        Affectation a = repo.findById(affectationId)
            .orElseThrow(() -> new NotFoundException("Affectation " + affectationId + " introuvable"));
        appliquer(a, req);
        return repo.save(a);
    }

    public void supprimer(Long affectationId) {
        if (!repo.existsById(affectationId)) {
            throw new NotFoundException("Affectation " + affectationId + " introuvable");
        }
        repo.deleteById(affectationId);
    }

    private void appliquer(Affectation a, AffectationRequest req) {
        a.setRoleCode(req.roleCode().name());
        a.setSaison(req.saison());
        a.setEquipe(req.equipe());
        a.setCommentaire(req.commentaire());
    }
}