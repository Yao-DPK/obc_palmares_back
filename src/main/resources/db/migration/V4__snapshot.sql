CREATE TABLE personne_snapshot (
    id           BIGSERIAL PRIMARY KEY,
    personne_id  BIGINT NOT NULL REFERENCES personne(id) ON DELETE CASCADE,
    payload      TEXT NOT NULL,
    modifie_par  VARCHAR(100) NOT NULL,
    cree_le      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_snapshot_personne ON personne_snapshot(personne_id, cree_le DESC);