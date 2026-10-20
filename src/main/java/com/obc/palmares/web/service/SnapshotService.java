package com.obc.palmares.web.service;

import java.util.List;

import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.obc.palmares.domain.Personne;
import com.obc.palmares.domain.PersonneSnapshot;
import com.obc.palmares.domain.PersonneSnapshotRepository;

import tools.jackson.databind.ObjectMapper;

@Service 
public class SnapshotService {
    
    public static final int MAX_SNAPSHOTS = 3;
    private final PersonneSnapshotRepository repo;
    private final ObjectMapper mapper;

    public SnapshotService(PersonneSnapshotRepository repo, ObjectMapper mapper){
        this.repo = repo;
        this.mapper = mapper;
    }

        @Transactional
    public void capturerAvantModification(Personne p, String auteur) {
        String payload;
        try {
            payload = mapper.writeValueAsString(p);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Snapshot impossible", e);
        }

        PersonneSnapshot snap = new PersonneSnapshot();
        snap.setPersonneId(p.getId());
        snap.setPayload(payload);
        snap.setModifiePar(auteur);
        repo.save(snap);

        List<PersonneSnapshot> existants =
            repo.findByPersonneIdOrderByCreeLeDesc(p.getId());

        // Garde les 3 plus récents
        for (int i = MAX_SNAPSHOTS; i < existants.size(); i++) {
            repo.delete(existants.get(i));
        }
    }

     
}
