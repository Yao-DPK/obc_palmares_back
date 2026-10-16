package com.obc.palmares.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class PersonneRepositoryImpl implements PersonneRepositoryCustom {

    private final JdbcTemplate jdbc;

    public PersonneRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Personne> rechercherSimilaireIndexe(String requete, double seuil, int limite) {
        // SET LOCAL ne s'applique que dans la transaction courante.
        // Le @Transactional sur la méthode appelante (SimilariteService) garantit
        // que ce SET et le SELECT ci-dessous partagent la même transaction.
        // Valeur constante du code, pas d'injection SQL possible.
        jdbc.execute("SET LOCAL pg_trgm.similarity_threshold = " + seuil);

        return jdbc.query("""
            SELECT id, nom, prenom, nom_normalise, date_naissance,
                   photo_url, bio, visibilite, statut, cree_le, maj_le
            FROM personne
            WHERE nom_normalise % ?
            ORDER BY similarity(nom_normalise, ?) DESC
            LIMIT ?
            """,
            MAPPER,
            requete, requete, limite
        );
    }

    private static final RowMapper<Personne> MAPPER = (rs, i) -> {
        Personne p = new Personne();
        p.setId(rs.getLong("id"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setNomNormalise(rs.getString("nom_normalise"));

        Date d = rs.getDate("date_naissance");
        p.setDateNaissance(d == null ? null : d.toLocalDate());

        p.setPhotoUrl(rs.getString("photo_url"));
        p.setBio(rs.getString("bio"));
        p.setVisibilite(Personne.Visibilite.valueOf(rs.getString("visibilite")));
        p.setStatut(Statut.valueOf(rs.getString("statut")));

        Timestamp cree = rs.getTimestamp("cree_le");
        Timestamp maj  = rs.getTimestamp("maj_le");
        p.setCreeLe(cree.toInstant());
        p.setMajLe(maj.toInstant());

        return p;
    };
}