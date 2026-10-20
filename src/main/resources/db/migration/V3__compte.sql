CREATE TABLE compte (
    id                 BIGSERIAL PRIMARY KEY,
    personne_id        BIGINT NOT NULL UNIQUE REFERENCES personne(id) ON DELETE CASCADE,
    username           VARCHAR(50) NOT NULL UNIQUE,
    password_hash      VARCHAR(100) NOT NULL,
    email              VARCHAR(200) NOT NULL,
    email_confirme_le  TIMESTAMPTZ,
    cree_le            TIMESTAMPTZ NOT NULL DEFAULT now(),
    derniere_connexion TIMESTAMPTZ
);

