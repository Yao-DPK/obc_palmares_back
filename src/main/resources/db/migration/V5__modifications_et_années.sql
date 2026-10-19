ALTER TABLE personne ADD COLUMN consentement_rgpd_le TIMESTAMPTZ;
ALTER TABLE personne ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'SELF_SERVICE';

ALTER TABLE affectation ADD COLUMN annee_debut INT;
ALTER TABLE affectation ADD COLUMN annee_fin INT;
ALTER TABLE affectation ALTER COLUMN saison DROP NOT NULL;
-- Pas de colonne categorie : formé et joueur sont deux statuts distincts.

ALTER TABLE role_type ADD COLUMN ordre INT NOT NULL DEFAULT 0;
INSERT INTO role_type (code, libelle, ordre) VALUES
    ('FORME', 'Formé', 1)
ON CONFLICT (code) DO NOTHING;

ALTER TABLE role_type ADD COLUMN ordre INT NOT NULL DEFAULT 0;

CREATE TABLE jeton_modification (
    token        VARCHAR(64) PRIMARY KEY,
    personne_id  BIGINT NOT NULL REFERENCES personne(id) ON DELETE CASCADE,
    payload      TEXT NOT NULL,
    expire_le    TIMESTAMPTZ NOT NULL,
    utilise_le   TIMESTAMPTZ,
    cree_le      TIMESTAMPTZ NOT NULL DEFAULT now()
);