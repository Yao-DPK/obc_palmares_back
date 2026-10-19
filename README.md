# OBC Palmarès

Registre d'archives pour un club sportif de 25 ans : joueurs, encadreurs,
dirigeants et personnel. L'objectif est de reconstituer l'histoire du club
à partir de données éparses (papier, Excel, mémoire des anciens) et de
publier un palmarès vérifiable.

## Le problème

Le club ne dispose d'aucun référentiel centralisé. Chaque saison qui passe
voit disparaître des informations : feuilles d'effectif perdues, orthographes
divergentes, photos non indexées. Personne ne peut répondre à *"qui jouait
en 1997 ?"* sans fouiller les archives personnelles de deux ou trois anciens.

Le projet vise à centraliser, fiabiliser et valoriser cet historique.

## Stack

- **Java 21** + **Spring Boot 3.3**
- **PostgreSQL 16** avec l'extension `pg_trgm`
- **Flyway** pour les migrations
- **Spring Data JPA** pour le CRUD, **JdbcTemplate** pour les lectures chaudes
- **Testcontainers** pour les tests d'intégration
- **Maven**

## Modélisation

Une personne ne *devient* pas encadreur. Elle **cumule des rôles datés**
sur 25 ans. C'est pourquoi le modèle sépare :

```
Personne ──< Affectation >── RoleType
```

- `Personne` : identité stable (nom, prénom, date de naissance, visibilité)
- `Affectation` : un rôle dans une période (`role`, `saison`, `equipe`)
- `RoleType` : référentiel des rôles (JOUEUR, ENCADREUR, DIRIGEANT, PERSONNEL)

Ce choix évite trois écueils : le JSON fourre-tout, les colonnes booléennes
qui explosent, et l'impossibilité de répondre à *"qui est passé de joueur à
encadreur ?"* sans changer le schéma.

## Recherche et détection de doublons

Sur 25 ans de saisie manuelle par des bénévoles, les doublons sont la norme,
pas l'exception : `Kouassi Yao`, `Yao Kouassi`, `K. Yao`, `Yao Kouacy`.
Détecter ces variantes est le cœur technique du projet.

### Stratégie à deux étages

**1. Filtrage côté base.** PostgreSQL avec l'index GIN trigram (`pg_trgm`)
retourne les 20 candidats les plus proches d'une requête normalisée.

**2. Scoring côté Java.** Chaque candidat est réévalué par un score pondéré :

- `0.9 × max(Levenshtein.ratio, JaroWinkler.similarite)`
- `+ 0.1 × signal_date` (1.0 si dates égales, 0.0 si différentes,
  0.5 si l'une des deux manque)

Levenshtein est précis sur les chaînes proches ; Jaro-Winkler capture les
fautes de frappe en fin de mot. Le `max` retient le meilleur signal plutôt
qu'une moyenne qui écrase les cas extrêmes.

### Normalisation

Stockée en colonne `nom_normalise`, calculée à l'écriture :
minuscules, accents retirés via `Normalizer.NFD`, ordre fixe `prenom nom`.
Stocker cette valeur est indispensable : sans elle, l'index GIN ne peut pas
être utilisé. C'est un compromis de dénormalisation assumé.

 Performances mesurées

Benchmark sur 200 requêtes par palier, noms majoritairement distincts
(suffixe incrémental tous les 400 enregistrements pour éviter le pool
artificiel de noms qui fausse la sélectivité de l'index).

Comparaison entre :

- **Naïf** : chargement complet en mémoire, comparaison linéaire
- **Indexé** : `pg_trgm` + index GIN + scoring Java

| Fiches | Naïf (ms/req) | Indexé (ms/req) | Mémoire naïf | Vainqueur |
|-------:|--------------:|----------------:|-------------:|:---------:|
|    500 |          0,65 |            1,62 |       0,2 Mo | Naïf ×2,5 |
|  5 000 |          3,69 |            6,87 |       0,3 Mo | Naïf ×1,9 |
| 20 000 |         14,45 |           24,52 |       1,3 Mo | Naïf ×1,7 |
| 50 000 |         36,50 |           19,02 |       3,1 Mo | **Indexé ×1,9** |

### Ce que ces chiffres m'ont appris

À petite échelle, l'approche mémoire gagne. Le coût fixe d'un aller-retour
JDBC (~1 ms : ouverture de transaction, `SET LOCAL`, `SELECT`, commit)
domine le coût variable d'une comparaison en mémoire. À 500 fiches, l'index
coûte **2,5× plus cher** que le chargement complet en RAM.

Le croisement se situe **autour de 27 000 fiches**, entre les paliers 20 000
et 50 000. L'estimation vient du fait que le naïf coûte ~0,73 µs par fiche
et par requête (constant sur les 4 paliers), tandis que l'indexé se stabilise
autour de 19-20 ms après chauffe JIT. Division : `20 ms / 0,73 µs ≈ 27 400`.

Au-delà, l'index trigram gagne parce que :

1. Sa complexité est logarithmique ; la comparaison linéaire est en O(N).
2. Sa consommation mémoire est O(1) au lieu de O(N) — à 50 000 fiches, le
   naïf charge 3,1 Mo d'objets JPA à chaque démarrage d'application.

**Décision :** je conserve l'approche indexée malgré son coût à petite échelle.
OBC a aujourd'hui ~500 fiches, mais le même code doit tenir à 50 000 si
l'application est reprise par une fédération. La complexité algorithmique ne
se négocie pas quand la cible de volume est inconnue.

### Parallélisation : un faux gain

Test sur 2 000 candidats en mémoire :

| Approche | Temps |
|---|---:|
| Séquentiel | 1,08 ms |
| `parallelStream` | 2,72 ms |

Le `parallelStream` est **2,5× plus lent** que le séquentiel. Le travail
unitaire (comparaison de chaînes de ~15 caractères) est trop petit pour
amortir le coût de dispatch du ForkJoinPool. La parallélisation serait
justifiée avec des chaînes beaucoup plus longues ou un volume 10× supérieur.
**Je ne l'ai pas utilisée.**

### Note sur la mesure

Les chiffres varient de ±10 % entre deux exécutions. Les valeurs du tableau
proviennent d'un run unique ; le point de croisement est une estimation, pas
une mesure exacte. Le but de ce benchmark n'est pas la précision au
centième, mais de valider une **décision d'architecture** : à quelle échelle
mon intuition se retourne-t-elle ?
## Décisions d'architecture notables

- **Pas de Kafka.** Aucun consommateur asynchrone. Une transaction DB suffit
  pour l'audit.
- **Pas d'Elasticsearch.** `pg_trgm` + index GIN répond en < 2 ms sur
  5 000 lignes. Ajouter un cluster serait de la dette opérationnelle gratuite.
- **Pas de microservices.** Un seul déployable, une seule équipe, aucun
  besoin de scaling indépendant.
- **Pas de Spring Batch.** Imports ponctuels de quelques centaines de lignes.
  Un `@Transactional` sur une boucle suffit.
- **Pas de Keycloak.** Spring Security + JWT, trois rôles, ~50 lignes.

Ces choix sont documentés parce qu'une architecture, c'est du compromis,
pas de l'accumulation.

## Lancer le projet

```bash
docker compose up -d
./mvnw spring-boot:run
```

L'API est exposée sur `http://localhost:8080/api`.

## Tests

```bash
./mvnw test
```

Le benchmark n'est pas lancé par défaut (il prend plusieurs minutes).
Pour l'exécuter :

```bash
./mvnw test -Dtest=SimilariteBenchmarkTest
```

## Endpoints principaux

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/personnes?page=0&size=20` | Liste paginée |
| GET | `/api/personnes/{id}` | Détail d'une personne |
| GET | `/api/personnes/recherche?q=yao+kouassi` | Recherche floue |
| POST | `/api/personnes` | Création directe |
| POST | `/api/personnes/soumettre` | Création avec détection de doublons |
| PUT | `/api/personnes/{id}` | Modification |
| DELETE | `/api/personnes/{id}` | Suppression |
| GET | `/api/personnes/{id}/affectations` | Historique des rôles |
| POST | `/api/personnes/{id}/affectations` | Ajout d'un rôle |
| PUT | `/api/affectations/{id}` | Modification d'un rôle |
| DELETE | `/api/affectations/{id}` | Suppression d'un rôle |

### Exemple — détection de doublons

```bash
# Première création
curl -X POST localhost:8081/api/personnes/soumettre \
  -H 'Content-Type: application/json' \
  -d '{"nom":"Kouassi","prenom":"Yao","dateNaissance":"1985-04-12"}'
# → 201 CREATED

# Faute de frappe
curl -X POST localhost:8081/api/personnes/soumettre \
  -H 'Content-Type: application/json' \
  -d '{"nom":"Kouacy","prenom":"Yao","dateNaissance":"1985-04-12"}'
# → 409 CONFLICT avec la liste des candidats et leurs scores

# Forcer la création malgré la suspicion
curl -X POST 'localhost:8081/api/personnes/soumettre?forcer=true' \
  -H 'Content-Type: application/json' \
  -d '{"nom":"Kouacy","prenom":"Yao","dateNaissance":"1985-04-12"}'
# → 201 CREATED
```

## Roadmap

- [x] CRUD personnes et affectations
- [x] Recherche floue avec index trigram
- [x] Détection de doublons avec scoring pondéré
- [x] Benchmark naïf vs indexé
- [ ] Frontend (formulaire de contribution, file de modération)
- [ ] Authentification et rôles (contributeur, modérateur, admin)
- [ ] Workflow de contribution → validation → fusion
- [ ] Import batch depuis Excel
- [ ] Gestion des médias (photos d'archives)
- [ ] Export RGPD