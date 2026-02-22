# Microservices Mini Project (Hospital Management)

Projet de mini plateforme hospitalière en architecture microservices avec Spring Boot / Spring Cloud.

## Fonctionnalités

- Gestion des patients (`patient-service`)
- Gestion des rendez-vous (`appointment-service`)
- Gestion des dossiers médicaux + historique (`medical-record-service`)
- API Gateway (`api-gateway`)
- Service Discovery avec Eureka (`eureka-server`)
- Configuration centralisée avec Spring Cloud Config (`config-server` + `config-repo`)
- Résilience inter-services avec Circuit Breaker (Resilience4j)

## Architecture

### Microservices métiers

- `patient-service`
- `appointment-service`
- `medical-record-service`

### Services d’infrastructure

- `config-server`
- `eureka-server`
- `api-gateway`
- `config-repo` (fichiers de configuration externalisés)

### Communication

- Client -> `api-gateway`
- `api-gateway` -> services via Eureka (`lb://...`)
- `appointment-service` -> `patient-service` via OpenFeign
- `medical-record-service` -> `patient-service` via OpenFeign
- Appels Feign protégés par circuit breaker (`patientService`)

## Stack Technique

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Maven (multi-modules)
- Spring Web
- Spring Data JPA
- H2
- Eureka Client/Server
- Spring Cloud Config
- Spring Cloud Gateway
- OpenFeign
- Resilience4j (Circuit Breaker)
- Actuator
- Lombok

## Structure du projet

```text
.
├── api-gateway
├── appointment-service
├── config-repo
├── config-server
├── eureka-server
├── medical-record-service
├── patient-service
├── pom.xml
├── start-all-services.sh
└── stop-all-services.sh
```

## Ports (configuration actuelle)

- `api-gateway` -> `8080`
- `patient-service` -> `8081`
- `appointment-service` -> `8082`
- `medical-record-service` -> `8083`
- `eureka-server` -> `8761`
- `config-server` -> `8888`

## Prérequis

- Java 17+
- Maven (`mvn`)
- `nc` (netcat) pour les scripts de démarrage/arrêt

## Démarrage rapide

### Option 1 (recommandée) : scripts fournis

Démarrer toute la stack :

```bash
./start-all-services.sh
```

Arrêter toute la stack :

```bash
./stop-all-services.sh
```

Les scripts :
- démarrent les services dans le bon ordre
- attendent l’ouverture des ports
- écrivent les logs dans `.run/logs/`
- stockent les PID dans `.run/pids/`

### Option 2 : démarrage manuel

Ordre recommandé :

1. `config-server`
2. `eureka-server`
3. `patient-service`
4. `appointment-service`
5. `medical-record-service`
6. `api-gateway`

Exemple :

```bash
mvn -pl config-server spring-boot:run
```

## Vérifications rapides

```bash
curl -i http://localhost:8761
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/actuator/gateway/routes
```

## Routes API Gateway

Configurées dans `config-repo/api-gateway.yml` :

- `/api/v1/patients/**` -> `PATIENT-SERVICE`
- `/api/v1/appointments/**` -> `APPOINTMENT-SERVICE`
- `/api/v1/medical-records/**` -> `MEDICAL-RECORD-SERVICE`

## Endpoints principaux (via Gateway)

### Patients

- `POST /api/v1/patients`
- `GET /api/v1/patients`
- `GET /api/v1/patients/{id}`
- `PUT /api/v1/patients/{id}`
- `DELETE /api/v1/patients/{id}`

### Appointments

- `POST /api/v1/appointments`
- `GET /api/v1/appointments`
- `GET /api/v1/appointments/patient/{patientId}`
- `PUT /api/v1/appointments/{id}`

### Medical Records

- `POST /api/v1/medical-records`
- `GET /api/v1/medical-records/patient/{patientId}`
- `POST /api/v1/medical-records/patient/{patientId}/entries`
- `GET /api/v1/medical-records/{recordId}`

## Exemples `curl`

### 1) Créer un patient

```bash
curl -i -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{"name":"Doe","firstName":"John","birthDate":"1995-06-15","contact":"0600000000"}'
```

### 2) Créer un rendez-vous

```bash
curl -i -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-02-22","time":"10:30:00","patientId":1}'
```

### 3) Créer un dossier médical

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"bloodType":"O+","allergies":"Penicillin"}'
```

### 4) Ajouter une entrée au dossier

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records/patient/1/entries \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-02-22","type":"CONSULTATION","content":"Routine follow-up visit"}'
```

### 5) Lire le dossier médical d’un patient

```bash
curl -i http://localhost:8080/api/v1/medical-records/patient/1
```

## Règles métier (Medical Record Service)

- Impossible de créer un dossier médical pour un patient inexistant
- Un patient possède au plus **un** dossier médical
- Une entrée d’historique doit être rattachée à un dossier existant

## Résilience (Circuit Breaker)

Les appels vers `patient-service` depuis :
- `appointment-service`
- `medical-record-service`

sont protégés par **Resilience4j Circuit Breaker**.

### Comportement attendu

- Si `patient-service` tombe en panne :
  - les premiers appels échouent (timeout/connexion)
  - après dépassement du seuil d’échec, le circuit s’ouvre
  - les appels suivants échouent rapidement avec `503 Service Unavailable`

### Vérification (exemple)

1. Démarrer toute la stack
2. Arrêter `patient-service`
3. Appeler un endpoint dépendant du patient

```bash
curl -i -X POST http://localhost:8080/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":1}'
```

## Build

Compiler un service :

```bash
mvn -pl medical-record-service -DskipTests compile
```

Compiler tout le projet :

```bash
mvn -DskipTests compile
```

## Rapport

Le rapport détaillé du projet est disponible dans :

- `RAPPORT_MICROSERVICES.md`

Vous pouvez l’exporter en PDF avec Pandoc :

```bash
pandoc RAPPORT_MICROSERVICES.md -o Rapport_Microservices.pdf
```

## Git

Le projet inclut un `.gitignore` pour :
- outputs Maven (`target/`)
- logs
- fichiers runtime (`.run/`)
- fichiers IDE (`.vscode/`, `.idea/`)

## Améliorations possibles

- `@ControllerAdvice` global pour centraliser les erreurs
- tests unitaires / intégration
- Docker Compose
- authentification via API Gateway (JWT/OAuth2)
- observabilité (Prometheus/Grafana, tracing)
