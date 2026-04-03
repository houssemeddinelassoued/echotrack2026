# Document de Contexte : Projet EcoTrack
  
**Type d'application :** SaaS B2B (Software as a Service)  
**Domaine :** Green IT / Numérique Responsable  
**Statut :** MVP (Minimum Viable Product) en phase de conception globale

---

## 1. Vision et Mission

La transformation numérique des entreprises s'accompagne d'une augmentation significative de leur empreinte carbone. La mission d'**EcoTrack** est de fournir aux organisations une solution clé en main pour **calculer, analyser et réduire leur empreinte carbone numérique**.

L'application agit comme un tableau de bord centralisé permettant aux décideurs (DSI, Responsables RSE) d'avoir une visibilité granulaire sur la consommation énergétique de leur parc informatique et de prendre des décisions éclairées pour optimiser leurs ressources.

## 2. Objectifs du MVP

Pour sa première itération, le produit se concentre sur les fonctionnalités fondamentales suivantes :
*   **Inventaire des équipements :** Cartographier le matériel IT (Serveurs, Laptops, Écrans, Smartphones) déployé au sein de l'entreprise.
*   **Suivi énergétique :** Collecter et stocker les relevés de consommation électrique journaliers de chaque équipement.
*   **Conversion Carbone :** Traduire la consommation électrique (kWh) en équivalent CO2 (gCO2e) afin de fournir des métriques lisibles et exploitables.
*   **Architecture Multitenant :** Garantir une isolation stricte et sécurisée des données pour chaque entreprise cliente (locataire) sur la plateforme.

## 3. Modélisation des Données (Conception Conceptuelle)

L'architecture des données repose sur une modélisation relationnelle robuste, pensée en amont (paradigme UML) pour assurer l'intégrité et l'évolutivité du système. Le cœur du système s'articule autour de quatre entités principales :

1.  **COMPANY (Entreprise) :** L'entité centrale du modèle multitenant. Elle représente le client B2B souscrivant au SaaS.
2.  **EMPLOYEE (Employé) :** Représente les collaborateurs rattachés à une entreprise.
3.  **ASSET (Équipement IT) :** Le matériel physique. Un *Asset* appartient obligatoirement à une entreprise, mais son assignation à un employé spécifique est facultative (permettant de gérer à la fois les ordinateurs personnels et les serveurs d'infrastructure).
4.  **DAILY_CONSUMPTION (Série temporelle) :** L'historique immuable des relevés. Cette entité stocke la consommation énergétique et l'empreinte carbone calculée par jour et par équipement, constituant la base de données analytique du SaaS.

## 4. Périmètre Fonctionnel (User Stories Principales)

*   **En tant qu'Admin Entreprise**, je peux créer et gérer les profils de mes collaborateurs.
*   **En tant qu'Admin Entreprise**, je peux ajouter, modifier ou retirer des équipements IT de mon inventaire.
*   **En tant qu'Admin Entreprise**, je peux assigner du matériel à un collaborateur précis ou le définir comme matériel partagé/infrastructure.
*   **En tant que Responsable RSE**, je peux visualiser l'empreinte carbone totale de mon entreprise sur une période donnée (agrégation des données temporelles).
*   **En tant que DSI**, je peux identifier les catégories d'équipements les plus énergivores pour cibler mes actions de réduction.

## 5. Perspectives et Évolutions (Post-MVP)

Une fois le MVP consolidé et la collecte de données fiabilisée, l'architecture prévoit l'intégration de modules avancés :
*   **Intelligence Artificielle & Machine Learning :** Exploitation de l'historique des séries temporelles (Deep Learning) pour la détection d'anomalies de consommation et l'analyse prédictive de l'empreinte carbone future.
*   **Intégration d'API tierces :** Connexion dynamique à des bases de données de référence (ex: Boavizta, ADEME) pour affiner les facteurs d'émission en temps réel.
*   **Recommandations automatisées :** Suggestions d'optimisation du parc informatique basées sur l'analyse des cycles de vie des équipements.