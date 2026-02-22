1 Objectif
L’objectif de ce mini-projet est de concevoir et implémenter une application reposant sur une
architecture microservices, en utilisant Spring Boot et Spring Cloud.
Les objectifs pédagogiques sont :
— Comprendre la décomposition d’un système en microservices
— Mettre en œuvre la découverte de services
— Centraliser l’accès via une API Gateway
— Implémenter la résilience dans un système distribué
2 Contexte fonctionnel

Le système développé représente une version simplifiée d’un système de gestion hospita-
lière permettant :

— la gestion des patients,
— la planification des rendez-vous médicaux,
— la gestion des dossiers médicaux.

L’architecture doit rester tolérante aux pannes et opérationnelle même en cas d’indispo-
nibilité partielle.

3 Architecture globale
Vue générale de l’architecture
L’application est composée de microservices métiers autonomes, soutenus par des services
d’infrastructure assurant la communication, la configuration et la résilience.
3.1 Microservices métiers
— Patient Service
— Appointment Service
— Medical Record Service
3.2 Services d’infrastructure
— Eureka Server (Service Discovery)
— API Gateway (Spring Cloud Gateway)
— Config Server

1

4 Description détaillée des microservices
4.1 Patient Service
Responsabilité principale :
— Gestion des informations administratives des patients
Fonctionnalités exposées :
— Création d’un patient
— Consultation d’un patient
— Liste des patients
Attributs principaux :
— id
— nom
— prénom
— date de naissance
— contact
Dépendances :
— Aucune (service autonome)
4.2 Appointment Service
Responsabilité principale :
— Gestion des rendez-vous médicaux
Fonctionnalités exposées :
— Création d’un rendez-vous
— Association d’un rendez-vous à un patient
— Consultation des rendez-vous d’un patient
Dépendances fonctionnelles :
— Patient Service (vérification de l’existence du patient)
Type de communication :
— Appel REST via API Gateway
— Découverte dynamique via Eureka
4.3 Medical Record Service
Responsabilité principale :
— Gestion des dossiers médicaux des patients
Fonctionnalités exposées :
— Création d’un dossier médical
— Ajout de diagnostics
— Consultation de l’historique médical
Dépendances fonctionnelles :
— Patient Service (association dossier–patient)

2

5 Dépendances inter-services
Résumé des dépendances
— API Gateway → Tous les microservices
— Appointment Service → Patient Service
— Medical Record Service → Patient Service
— Tous les services → Eureka Server
— Tous les services → Config Server
La communication repose sur :
— Protocoles REST
— Format JSON
— Routage centralisé via l’API Gateway
— Découverte automatique des services avec Eureka
6 Résilience
La résilience est assurée par Spring Cloud Circuit Breaker et Resilience4j.
6.1 Mécanismes implémentés
— Circuit Breaker
— Timeout
— Retry
— Fallback
6.2 Scénarios de défaillance
— Indisponibilité des services
— Latence élevée
— Erreurs transitoires
Dans ces cas, le système doit garantir une dégradation contrôlée sans arrêt global.
7 Technologies utilisées

Domaine Technologie
Langage Java
Framework Spring Boot
Service Discovery Eureka
API Gateway Spring Cloud Gateway
Communication REST, OpenFeign
Résilience Resilience4j
Base de données H2 / MySQL
Build Maven
