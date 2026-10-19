CREATE TABLE invitation (
    token         VARCHAR(64) PRIMARY KEY,
    email         VARCHAR(200) NOT NULL,
    personne_id   BIGINT REFERENCES personne(id),
    expire_le     TIMESTAMPTZ NOT NULL,
    consomme_le   TIMESTAMPTZ,
    cree_le       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitation_personne ON invitation(personne_id);