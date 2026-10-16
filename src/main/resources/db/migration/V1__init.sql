CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE personne (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    nom_normalise   VARCHAR(200) NOT NULL,
    date_naissance  DATE,
    photo_url       VARCHAR(500),
    bio             TEXT,
    visibilite      VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    statut          VARCHAR(20) NOT NULL DEFAULT 'ACTIF',
    cree_le         TIMESTAMPTZ NOT NULL DEFAULT now(),
    maj_le          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_personne_nom_trgm
    ON personne USING gin (nom_normalise gin_trgm_ops);

CREATE TABLE role_type (
    code        VARCHAR(30) PRIMARY KEY,
    libelle     VARCHAR(100) NOT NULL
);

INSERT INTO role_type (code, libelle) VALUES
    ('JOUEUR',     'Joueur'),
    ('ENCADREUR',  'Encadreur'),
    ('FORME',  'Formé'),
    ('PERSONNEL',  'Membre du personnel');

CREATE TABLE affectation (
    id           BIGSERIAL PRIMARY KEY,
    personne_id  BIGINT NOT NULL REFERENCES personne(id) ON DELETE CASCADE,
    role_code    VARCHAR(30) NOT NULL REFERENCES role_type(code),
    saison       VARCHAR(9) NOT NULL,
    equipe       VARCHAR(50),
    commentaire  TEXT,
    cree_le      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_affectation_personne ON affectation(personne_id);
CREATE INDEX idx_affectation_saison   ON affectation(saison);