<p align="center">
  <img src="https://img.shields.io/badge/🌱-EcoTrack-2ECC71?style=for-the-badge&logoColor=white" alt="EcoTrack Logo" height="60"/>
</p>

<h1 align="center">EcoTrack</h1>

<p align="center">
  <strong>🌍 Mesurez. Analysez. Réduisez. Votre empreinte carbone numérique.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"/>
  <img src="https://img.shields.io/badge/Architecture-DDD-purple?style=flat-square" alt="DDD"/>
  <img src="https://img.shields.io/badge/Model-Multi--Tenant-orange?style=flat-square" alt="Multi-Tenant"/>
</p>

<p align="center">
  <a href="#-pitch">Pitch</a> •
  <a href="#-stack-technique">Stack</a> •
  <a href="#️-installation-en-local">Installation</a> •
  <a href="#-lancement-via-docker">Docker</a> •
  <a href="#-lancement-des-tests">Tests</a> •
  <a href="#-guidelines-de-contribution">Contribuer</a>
</p>

---

## 🚀 Pitch

**EcoTrack** est une plateforme SaaS B2B qui permet aux entreprises de **calculer, analyser et réduire leur empreinte carbone numérique** en temps réel. En transformant automatiquement les données de consommation énergétique (kWh) en équivalent CO2 (gCO2e), EcoTrack offre une vision claire et actionnable de l'impact environnemental de votre infrastructure IT.

> *"Ce qui se mesure s'améliore."* — Avec EcoTrack, prenez le contrôle de votre responsabilité environnementale numérique.

---

## 🛠 Stack Technique

| Composant | Technologie | Description |
|-----------|-------------|-------------|
| **Langage** | ![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white) | LTS avec Records, Pattern Matching, Virtual Threads |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring-boot&logoColor=white) | Backend robuste et production-ready |
| **Architecture** | Domain-Driven Design | Séparation claire domain/application/infrastructure |
| **Base de données** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white) | Stockage relationnel multi-tenant |
| **Conteneurisation** | ![Docker](https://img.shields.io/badge/Docker-24-2496ED?logo=docker&logoColor=white) | Déploiement standardisé |
| **Tests** | ![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white) | Tests unitaires et d'intégration |

### 📁 Structure du projet

```
src/
├── main/java/com/ecotrack/carbonengine/
│   ├── application/          # Services, stratégies, ports
│   │   ├── factory/          # Factories pour les stratégies
│   │   ├── policy/           # Politiques métier (coefficients énergie)
│   │   ├── service/          # Services applicatifs
│   │   └── strategy/         # Stratégies de calcul carbone
│   ├── domain/               # Cœur métier
│   │   ├── enums/            # Types d'assets, sources d'énergie
│   │   ├── exception/        # Exceptions métier personnalisées
│   │   └── model/            # Entités et Value Objects
│   ├── infrastructure/       # Implémentations techniques
│   │   ├── config/           # Configuration et providers
│   │   └── parser/           # Parsers de logs serveur
│   └── web/                  # Couche présentation REST
│       ├── controller/       # Endpoints API
│       ├── dto/              # Objets de transfert
│       └── mapper/           # Mappeurs entité ↔ DTO
└── test/                     # Tests unitaires et d'intégration
```

---

## ⚙️ Installation en local

### Prérequis

- ☕ **Java 21** (OpenJDK ou Temurin)
- 📦 **Maven 3.9+**
- 🐘 **PostgreSQL 16** (optionnel, H2 en mode dev)

### Étapes d'installation

```bash
# 1. Cloner le repository
git clone https://github.com/houssemeddinelassoued/echotrack2026.git
cd echotrack2026

# 2. Vérifier la version Java
java -version
# Doit afficher : openjdk version "21.x.x"

# 3. Installer les dépendances
mvn clean install -DskipTests

# 4. Lancer l'application
mvn spring-boot:run

# 🎉 L'API est disponible sur http://localhost:8080
```

### Variables d'environnement (optionnel)

```bash
# Configuration base de données
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecotrack
export SPRING_DATASOURCE_USERNAME=ecotrack_user
export SPRING_DATASOURCE_PASSWORD=your_secure_password

# Configuration applicative
export ECOTRACK_DEFAULT_COUNTRY=FR
export ECOTRACK_CO2_COEFFICIENT=0.0569  # gCO2e/kWh pour la France
```

---

## 🐳 Lancement via Docker

### Option 1 : Docker Compose (recommandé)

```bash
# Lancer l'application avec sa base de données
docker-compose up -d

# Vérifier les logs
docker-compose logs -f ecotrack-api

# Arrêter les services
docker-compose down
```

### Option 2 : Image Docker seule

```bash
# Construire l'image
docker build -t ecotrack:latest .

# Lancer le conteneur
docker run -d \
  --name ecotrack-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/ecotrack \
  ecotrack:latest

# Vérifier que le conteneur est en cours d'exécution
docker ps | grep ecotrack
```

### 📋 docker-compose.yml

```yaml
version: '3.8'
services:
  ecotrack-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ecotrack
    depends_on:
      - db
    
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ecotrack
      POSTGRES_USER: ecotrack_user
      POSTGRES_PASSWORD: ecotrack_secret
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres_data:
```

---

## 🧪 Lancement des tests

```bash
# Exécuter tous les tests
mvn test

# Tests avec rapport de couverture
mvn test jacoco:report
# Rapport disponible dans: target/site/jacoco/index.html

# Tests d'un module spécifique
mvn test -Dtest=CarbonCalculationEngineTest

# Tests d'intégration uniquement
mvn verify -DskipUnitTests

# Mode watch (re-exécute les tests à chaque modification)
mvn fizzed-watcher:run
```

### 📊 Structure des tests

| Catégorie | Localisation | Description |
|-----------|--------------|-------------|
| **Unitaires** | `src/test/.../domain/` | Tests des entités et règles métier |
| **Services** | `src/test/.../application/` | Tests des services applicatifs |
| **API** | `src/test/.../web/` | Tests des controllers REST |
| **Infrastructure** | `src/test/.../infrastructure/` | Tests des parsers et providers |

---

## 🤝 Guidelines de contribution

Nous accueillons chaleureusement les contributions ! 🎉

### Comment contribuer

1. **🍴 Fork** le repository
2. **🌿 Créez** une branche pour votre feature
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **💻 Développez** en respectant les conventions
4. **✅ Testez** vos modifications
   ```bash
   mvn test
   ```
5. **📝 Commitez** avec un message clair
   ```bash
   git commit -m "feat: add amazing feature for carbon calculation"
   ```
6. **🚀 Pushez** et créez une **Pull Request**

### 📏 Conventions de code

| Règle | Description |
|-------|-------------|
| **SOLID** | Appliquer les principes SOLID systématiquement |
| **Nommage** | Noms explicites en anglais (variables, méthodes, classes) |
| **Méthodes** | Courtes, une seule responsabilité |
| **Javadoc** | Obligatoire sur toutes les méthodes publiques |
| **Tests** | Couverture minimale de 80% sur le nouveau code |
| **Multi-tenant** | Toujours respecter l'isolation des données par tenant |

### 📋 Convention de commits

Nous utilisons [Conventional Commits](https://www.conventionalcommits.org/) :

```
feat:     Nouvelle fonctionnalité
fix:      Correction de bug
docs:     Documentation uniquement
style:    Formatage (n'affecte pas le code)
refactor: Refactoring sans changement fonctionnel
test:     Ajout ou modification de tests
chore:    Maintenance (build, CI, dépendances)
```

### 🐛 Signaler un bug

Utilisez les [GitHub Issues](https://github.com/houssemeddinelassoued/echotrack2026/issues) avec le template approprié.

---

## 📄 License

Ce projet est sous licence **MIT**. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

<p align="center">
  <strong>🌱 Ensemble, réduisons l'empreinte carbone du numérique 🌍</strong>
</p>

<p align="center">
  Fait avec ❤️ par l'équipe EcoTrack
</p>
