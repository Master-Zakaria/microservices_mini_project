# Rapport de Projet Microservices Hospitaliers

## 1. Introduction

Ce projet implémente une architecture microservices pour un mini système hospitalier avec :

- gestion des patients
- gestion des rendez-vous
- gestion des dossiers médicaux (historique médical)
- configuration centralisée
- découverte de services
- API Gateway
- résilience inter-services (Circuit Breaker)

L'objectif est de démontrer une architecture distribuée simple, extensible et robuste face aux pannes partielles.

## 2. Architecture Globale

### 2.1 Vue d'ensemble

L'architecture repose sur les composants suivants :

- `config-server` : centralisation de la configuration Spring Cloud Config
- `eureka-server` : découverte de services (service registry)
- `api-gateway` : point d’entrée unique pour les clients
- `patient-service` : gestion des patients
- `appointment-service` : gestion des rendez-vous
- `medical-record-service` : gestion des dossiers médicaux et des entrées d’historique
- `config-repo` : fichiers YAML de configuration externalisés

### 2.2 Flux de communication

- Le client appelle `api-gateway`
- `api-gateway` route la requête vers le microservice cible via Eureka (`lb://...`)
- `appointment-service` et `medical-record-service` communiquent avec `patient-service` via **OpenFeign**
- Ces appels Feign sont protégés par **Resilience4j Circuit Breaker**

### 2.3 Ports (configuration actuelle)

- `api-gateway` : `8080`
- `patient-service` : `8081`
- `appointment-service` : `8082`
- `medical-record-service` : `8083`
- `config-server` : `8888`
- `eureka-server` : `8761`

## 3. Description des Microservices

### 3.1 `patient-service`

Rôle :
- CRUD des patients

Fonctionnalités principales :
- création d’un patient
- consultation par ID
- mise à jour
- suppression
- liste des patients

Endpoint principal :
- `/api/v1/patients`

### 3.2 `appointment-service`

Rôle :
- gestion des rendez-vous médicaux

Fonctionnalités principales :
- création d’un rendez-vous
- consultation de tous les rendez-vous
- consultation des rendez-vous d’un patient
- mise à jour d’un rendez-vous

Communication inter-service :
- validation de l’existence du patient via `patient-service` (Feign)

Résilience :
- appel patient protégé par circuit breaker (`patientService`)

Endpoint principal :
- `/api/v1/appointments`

### 3.3 `medical-record-service`

Rôle :
- gestion des dossiers médicaux (dossier + entrées d’historique)

#### Modèles métier

**MedicalRecord** (dossier)
- `id`
- `patientId` (référence simple, sans jointure)
- `createdAt`
- `updatedAt`
- `bloodType` (optionnel)
- `allergies` (optionnel)

**RecordEntry** (événements du dossier)
- `id`
- `recordId`
- `date`
- `type` (`CONSULTATION`, `DIAGNOSIS`, `PRESCRIPTION`, `NOTE`)
- `content`

#### Règles métier implémentées

- **Règle A** : impossible de créer un dossier pour un patient inexistant (validation via Feign vers `patient-service`)
- **Règle B** : un patient possède au plus un dossier médical (unicité par `patientId`, erreur `409 Conflict`)
- **Règle C** : une entrée ne peut être créée que pour un dossier existant

Endpoints principaux :
- `POST /api/v1/medical-records`
- `GET /api/v1/medical-records/patient/{patientId}`
- `POST /api/v1/medical-records/patient/{patientId}/entries`
- `GET /api/v1/medical-records/{recordId}` (optionnel implémenté)

## 4. API Gateway

Le `api-gateway` sert de point d’entrée unique et masque les adresses/ports internes.

Routes configurées :
- `/api/v1/patients/**` -> `PATIENT-SERVICE`
- `/api/v1/appointments/**` -> `APPOINTMENT-SERVICE`
- `/api/v1/medical-records/**` -> `MEDICAL-RECORD-SERVICE`

Avantages :
- point d’accès unifié
- routage centralisé
- possibilité future d’ajouter auth, rate limiting, logging, etc.

## 5. Configuration Centralisée (Spring Cloud Config)

La configuration des microservices est externalisée dans `config-repo`.

Exemples :
- ports des services
- configuration gateway
- paramètres de résilience (Resilience4j)
- timeouts Feign

Avantages :
- séparation code / configuration
- maintenance simplifiée
- cohérence entre environnements (dev/test/prod)

## 6. Découverte de Services (Eureka)

`eureka-server` joue le rôle de registre de services.

Chaque microservice (et la gateway) s’enregistre auprès d’Eureka :
- découverte dynamique des instances
- routage load-balanced via `lb://SERVICE-NAME`

Avantages :
- pas besoin d’URLs fixes entre services
- extensibilité (plusieurs instances d’un même service)

## 7. Dépendances et Choix Technologiques

### 7.1 Stack principale

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1**
- **Maven** (multi-modules)

### 7.2 Dépendances utilisées

- `spring-boot-starter-web` : API REST
- `spring-boot-starter-data-jpa` : persistance
- `h2` : base de données embarquée (développement)
- `spring-cloud-starter-netflix-eureka-client` : Eureka client
- `spring-cloud-starter-config` : Config Client
- `spring-cloud-starter-openfeign` : appels HTTP inter-services (déclaratifs)
- `spring-cloud-starter-gateway` : API Gateway (Spring Cloud Gateway)
- `spring-boot-starter-actuator` : monitoring / endpoints d’administration
- `spring-cloud-starter-circuitbreaker-resilience4j` : circuit breaker
- `spring-boot-starter-aop` : support des annotations Resilience4j
- `lombok` : réduction du boilerplate Java

### 7.3 Justification des choix

- **Spring Boot / Cloud** : intégration native des patterns microservices (config, discovery, gateway, Feign)
- **Feign** : code plus lisible qu’un client HTTP manuel
- **Resilience4j** : standard moderne pour circuit breaker, plus léger qu’Hystrix
- **H2** : rapide pour démonstration et tests locaux
- **Maven multi-module** : gestion centralisée des versions et dépendances

## 8. Résilience : Circuit Breaker

### 8.1 Problème adressé

Si `patient-service` devient indisponible :
- les appels Feign depuis `appointment-service` / `medical-record-service` échouent
- sans protection, les appels peuvent se répéter lentement (timeouts) et dégrader tout le système

### 8.2 Solution implémentée

Un **Circuit Breaker Resilience4j** est appliqué autour des appels Feign au niveau d’un service dédié de lookup patient :

- `appointment-service`: `PatientLookupService`
- `medical-record-service`: `PatientLookupService`

Les appels sont annotés avec :
- `@CircuitBreaker(name = "patientService", fallbackMethod = "...")`

### 8.3 Comportement

- **Closed** : fonctionnement normal
- **Open** : les appels sont rejetés immédiatement (fast fail)
- **Half-Open** : quelques appels de test sont autorisés pour vérifier la reprise

### 8.4 Paramètres configurés (exemple)

- fenêtre glissante basée sur le nombre d’appels (`COUNT_BASED`)
- taille de fenêtre : `5`
- minimum d’appels avant calcul : `3`
- seuil d’échec : `50%`
- attente en état ouvert : `15s`
- appels de test en half-open : `2`

### 8.5 Gestion des erreurs

Le fallback distingue deux cas :

- **Patient inexistant** (`FeignException.NotFound`) :
  - ce n’est pas une panne infrastructure
  - l’erreur métier est conservée (ex. `400` / `404`)

- **Service indisponible / timeout / circuit ouvert** :
  - levée d’une exception métier technique (`PatientServiceUnavailableException`)
  - retour HTTP `503 Service Unavailable`

Cette distinction est importante pour ne pas confondre une vraie absence de patient avec une panne réseau/service.

## 9. Scénarios de Test

### 9.1 Préparation / démarrage

Scripts fournis à la racine du projet :

- `./start-all-services.sh` : démarre tous les services dans le bon ordre
- `./stop-all-services.sh` : arrête tous les services via PID files

### 9.2 Vérifications de base

```bash
curl -i http://localhost:8761
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/actuator/gateway/routes
```

### 9.3 Tests fonctionnels (via API Gateway)

#### A. Créer un patient

```bash
curl -i -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{"name":"Doe","firstName":"John","birthDate":"1995-06-15","contact":"0600000000"}'
```

#### B. Créer un rendez-vous pour un patient existant

```bash
curl -i -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-02-22","time":"10:30:00","patientId":1}'
```

#### C. Créer un dossier médical pour un patient existant

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"bloodType":"O+","allergies":"Penicillin"}'
```

#### D. Ajouter une entrée au dossier

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records/patient/1/entries \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-02-22","type":"CONSULTATION","content":"Routine follow-up visit"}'
```

#### E. Consulter le dossier avec son historique

```bash
curl -i http://localhost:8080/api/v1/medical-records/patient/1
```

### 9.4 Tests de règles métier

#### Test Règle A : patient inexistant

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":9999}'
```

Résultat attendu :
- erreur client (`400` ou `404` selon endpoint/traitement)

#### Test Règle B : un seul dossier par patient

Exécuter deux fois la création du dossier pour le même `patientId`.

Résultat attendu :
- 1ère requête : `201 Created`
- 2ème requête : `409 Conflict`

#### Test Règle C : entrée sans dossier

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records/patient/999/entries \
  -H "Content-Type: application/json" \
  -d '{"type":"NOTE","content":"Test"}'
```

Résultat attendu :
- `404 Not Found` (dossier introuvable)

### 9.5 Tests de résilience (Circuit Breaker)

#### Scénario : indisponibilité de `patient-service`

1. Démarrer toute la stack
2. Arrêter `patient-service` (ou `./stop-all-services.sh` puis relancer sans patient-service)
3. Appeler un endpoint dépendant de `patient-service` :

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":1}'
```

ou

```bash
curl -i http://localhost:8080/api/v1/appointments/patient/1
```

Résultats attendus :
- au début : erreurs liées à timeout/connexion
- après accumulation d’échecs : **réponse plus rapide** avec `503 Service Unavailable` (circuit ouvert)

#### Vérification via Actuator (si exposé)

```bash
curl -i http://localhost:8082/actuator/health
curl -i http://localhost:8083/actuator/health
```

Objectif :
- observer l’état des circuit breakers dans les détails de santé

## 10. Sécurité, Limites et Améliorations Possibles

### 10.1 Limites actuelles

- pas d’authentification / autorisation
- H2 en local (non adaptée à la production)
- gestion des exceptions encore partiellement dans les contrôleurs (try/catch)
- pas de tracing distribué / corrélation des logs
- pas de tests automatisés (unitaires / intégration) fournis dans cette version

### 10.2 Améliorations recommandées

- ajouter `@ControllerAdvice` global par service
- ajouter tests d’intégration (MockMvc, Testcontainers)
- migrer vers PostgreSQL/MySQL
- ajouter observabilité (Prometheus/Grafana, Zipkin/Tempo)
- sécuriser la gateway (JWT / OAuth2)
- ajouter retry + timeout + bulkhead (compléments au circuit breaker)
- déployer via Docker Compose / Kubernetes

## 11. Conclusion

Le projet met en œuvre une architecture microservices cohérente avec :

- configuration centralisée
- découverte de services
- routage via API Gateway
- communication inter-services via Feign
- résilience avec Circuit Breaker (Resilience4j)

La solution répond aux exigences fonctionnelles (patients, rendez-vous, dossiers médicaux) tout en intégrant des mécanismes de robustesse essentiels pour des systèmes distribués.

## 12. Annexes (Références utiles)

Fichiers importants du projet :

- `pom.xml` (parent multi-modules)
- `config-repo/api-gateway.yml`
- `config-repo/appointment-service.yml`
- `config-repo/medical-record-service.yml`
- `appointment-service/src/main/java/microservice/appointmentservice/controllers/AppointmentServiceController.java`
- `appointment-service/src/main/java/microservice/appointmentservice/services/PatientLookupService.java`
- `medical-record-service/src/main/java/microservice/medicalrecordservice/controllers/MedicalRecordServiceController.java`
- `medical-record-service/src/main/java/microservice/medicalrecordservice/services/PatientLookupService.java`
- `start-all-services.sh`
- `stop-all-services.sh`
