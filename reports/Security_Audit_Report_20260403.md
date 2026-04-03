# 🛡️ Rapport d'Audit de Sécurité - EcoTrack Carbon Engine

**Date**: 3 avril 2026  
**Auditeur**: Security Review Agent (AppSec)  
**Périmètre**: Module `carbon-engine` (src/main/java/com/ecotrack/carbonengine)  
**Méthodologie**: Analyse statique de code, patterns OWASP, validation multi-tenant

---

## 📊 Résumé Exécutif

| Métrique | Valeur |
|----------|--------|
| **Fichiers analysés** | 15 fichiers Java |
| **Vulnérabilités critiques** | 0 |
| **Vulnérabilités hautes** | 0 |
| **Vulnérabilités moyennes** | 2 |
| **Vulnérabilités basses** | 3 |
| **Améliorations recommandées** | 4 |

### Verdict Global: ✅ POSTURE SÉCURITAIRE SOLIDE

Le module `carbon-engine` présente une bonne hygiène de sécurité avec des validations d'entrées systématiques, une isolation tenant explicite, et une absence de patterns dangereux classiques.

---

## ✅ Points Forts (Bonnes Pratiques Détectées)

### 1. Isolation Multi-Tenant Robuste
```
Fichiers: TenantScopedEntity.java, Asset.java, CarbonFootprint.java
```
- `TenantScopedEntity` impose un `tenantId` obligatoire via `Objects.requireNonNull()`
- Toutes les entités métier héritent de cette classe abstraite
- Tests unitaires validant explicitement l'isolation tenant (`shouldMaintainTenantIsolation`)

### 2. Validation des Entrées Systématique
```
Fichiers: Asset.java, CarbonFootprint.java, EnergyConfiguration.java, CarbonCalculationEngine.java
```
- Méthodes `validateName()`, `validatePositive()`, `validateNonNegative()`, `validatePercentage()`
- Validation en couche service (`validateInputs()`)
- Normalisation des codes pays (`.trim().toUpperCase(Locale.ROOT)`)

### 3. Absence de Patterns Dangereux
| Pattern Recherché | Résultat |
|-------------------|----------|
| SQL Injection (`Statement`, `createStatement`) | ❌ Non trouvé |
| Command Injection (`Runtime.exec`, `ProcessBuilder`) | ❌ Non trouvé |
| Deserialization (`ObjectInputStream`, `readObject`) | ❌ Non trouvé |
| Path Traversal (`new File(userInput)`) | ❌ Non trouvé |
| Secrets Hardcodés (`password=`, `apiKey=`) | ❌ Non trouvé |
| Logging sensible | ❌ Non trouvé (aucun logger) |

### 4. Gestion d'Exceptions Structurée
- Hiérarchie d'exceptions custom: `AssetCalculationException` → `CalculationException`, `InvalidAssetDataException`, etc.
- Messages d'erreur clairs et non techniques pour les utilisateurs
- Encapsulation des exceptions runtime dans `CalculationException`

### 5. Immutabilité des Entités
- Classes déclarées `final`
- Champs `final` avec assignation unique au constructeur
- Pas de setters publics

---

## 🟡 Vulnérabilités Moyennes

### [MEDIUM] M-01: Absence de Fichier de Build - Dépendances Non Auditables

**CWE**: CWE-1104 (Use of Unmaintained Third Party Components)  
**OWASP**: A06:2021 (Vulnerable and Outdated Components)

**Description**:  
Aucun fichier `pom.xml` ou `build.gradle` n'a été trouvé dans le workspace. Impossible d'auditer les dépendances tierces pour des CVEs connues.

**Impact**:  
- Risque de dépendances vulnérables non détectées
- Pas de gestion centralisée des versions
- CI/CD potentiellement fragilisée

**Recommandation**:  
```xml
<!-- pom.xml minimal recommandé -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.ecotrack</groupId>
    <artifactId>carbon-engine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Déclarer explicitement les dépendances -->
    </dependencies>
    
    <!-- Plugin OWASP Dependency Check -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.owasp</groupId>
                <artifactId>dependency-check-maven</artifactId>
                <version>9.0.9</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### [MEDIUM] M-02: Exception Chain Exposure Potentielle

**Fichier**: [CarbonCalculationEngine.java](src/main/java/com/ecotrack/carbonengine/application/service/CarbonCalculationEngine.java#L96)  
**CWE**: CWE-209 (Generation of Error Message Containing Sensitive Information)  
**OWASP**: A05:2021 (Security Misconfiguration)

**Description**:  
L'exception `CalculationException` encapsule la cause originale (`RuntimeException exception`), ce qui peut exposer des détails internes si propagé à l'API.

**Code concerné**:
```java
} catch (RuntimeException exception) {
    throw new CalculationException("Unexpected error while calculating carbon footprint", exception);
}
```

**Impact**:  
Si l'exception remonte jusqu'à une réponse HTTP, le stacktrace pourrait révéler:
- Noms de classes internes
- Détails d'infrastructure (DB, chemins fichiers)
- Informations exploitables pour reconnaissance

**Recommandation**:  
Ajouter un handler au niveau API boundary qui:
1. Log l'exception complète côté serveur
2. Retourne un message générique côté client
3. Utilise un correlation ID pour traçabilité

```java
// Exemple de handler API (à implémenter dans la couche web)
@ExceptionHandler(CalculationException.class)
public ResponseEntity<ErrorResponse> handleCalculationException(
        CalculationException ex, HttpServletRequest request) {
    String correlationId = UUID.randomUUID().toString();
    log.error("Calculation error [correlationId={}]", correlationId, ex);
    return ResponseEntity.status(500)
        .body(new ErrorResponse("Calculation failed", correlationId));
}
```

---

## 🟢 Vulnérabilités Basses

### [LOW] L-01: Absence d'Infrastructure de Logging

**CWE**: CWE-778 (Insufficient Logging)  
**OWASP**: A09:2021 (Security Logging and Monitoring Failures)

**Description**:  
Aucune infrastructure de logging détectée (`Logger`, `LoggerFactory`, `@Slf4j`).

**Impact**:  
- Pas d'audit trail pour les opérations sensibles
- Difficulté à détecter des anomalies ou attaques
- Compliance potentiellement non atteinte (SOC2, ISO27001)

**Recommandation**:  
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CarbonCalculationEngine {
    private static final Logger log = LoggerFactory.getLogger(CarbonCalculationEngine.class);
    
    public CarbonFootprint calculateCarbonFootprint(...) {
        log.info("Calculating footprint for asset={} tenant={}", 
            asset.getId(), asset.getTenantId());
        // ...
        log.debug("Footprint calculated: energy={}kWh emission={}kgCO2e", 
            consumedEnergyKwh, adjustedEmissionKgCo2e);
    }
}
```

⚠️ **Important**: Ne jamais logger de données sensibles (passwords, tokens, PII).

---

### [LOW] L-02: Absence de Rate Limiting Architectural

**CWE**: CWE-770 (Allocation of Resources Without Limits)  
**OWASP**: A04:2021 (Insecure Design)

**Description**:  
Le service `CarbonCalculationEngine` n'a pas de mécanisme de protection contre les appels massifs.

**Impact**:  
- Risque de DoS par calculs intensifs répétés
- Consommation excessive de ressources
- Coûts potentiellement élevés en environnement cloud

**Recommandation**:  
Implémenter un rate limiter au niveau application ou API gateway:
```java
// Avec Resilience4j
@RateLimiter(name = "carbonCalculation", fallbackMethod = "rateLimitFallback")
public CarbonFootprint calculateCarbonFootprint(...) {
    // ...
}
```

---

### [LOW] L-03: Pas de Validation de Format CountryCode

**Fichier**: [CarbonCalculationEngine.java](src/main/java/com/ecotrack/carbonengine/application/service/CarbonCalculationEngine.java#L107)  
**CWE**: CWE-20 (Improper Input Validation)

**Description**:  
Le `countryCode` est validé pour non-nullité et non-blankness, mais pas pour le format ISO 3166-1 alpha-2.

**Code actuel**:
```java
if (countryCode == null || countryCode.isBlank()) {
    throw new CalculationException("Country code is required");
}
```

**Impact**:  
Des codes invalides comme "FRANCE" ou "12" passeraient la validation et échoueraient plus tard lors du lookup.

**Recommandation**:  
```java
private void validateInputs(final Asset asset, final int usageDurationDays, final String countryCode) {
    Objects.requireNonNull(asset, "Asset is required");
    if (usageDurationDays <= 0) {
        throw new CalculationException("Usage duration must be strictly positive");
    }
    if (countryCode == null || countryCode.isBlank()) {
        throw new CalculationException("Country code is required");
    }
    // Validation format ISO 3166-1 alpha-2
    if (!countryCode.matches("^[A-Za-z]{2}$")) {
        throw new CalculationException("Country code must be a valid ISO 3166-1 alpha-2 code");
    }
}
```

---

## ℹ️ Améliorations Recommandées (Bonnes Pratiques)

### [INFO] I-01: Ajouter des Annotations de Validation JSR-380

Utiliser `@NotNull`, `@Positive`, `@Size` pour une validation déclarative:
```java
public CarbonFootprint calculateCarbonFootprint(
        @NotNull Asset asset,
        @Positive int usageDurationDays,
        @NotBlank @Size(min = 2, max = 2) String countryCode
) { ... }
```

### [INFO] I-02: Implémenter des Tests de Sécurité

Ajouter des tests spécifiques pour la sécurité:
- Tests de boundary (valeurs limites, overflow)
- Tests de tenant isolation avec tentative de cross-tenant access
- Tests de validation avec payloads malicieux

### [INFO] I-03: Documenter les Décisions de Sécurité

Créer un fichier `SECURITY.md` documentant:
- Modèle de menaces (threat model)
- Décisions de design sécurité
- Procédure de signalement de vulnérabilités

### [INFO] I-04: Intégrer SAST/DAST en CI/CD

Configurer des outils d'analyse automatique:
- **SAST**: SonarQube, Semgrep, SpotBugs
- **Dependency Scan**: OWASP Dependency-Check, Snyk
- **DAST**: OWASP ZAP (quand l'API sera exposée)

---

## 📋 Checklist de Remédiation

| ID | Priorité | Action | Effort |
|----|----------|--------|--------|
| M-01 | 🟠 Moyenne | Créer pom.xml + OWASP Dependency Check | 2h |
| M-02 | 🟠 Moyenne | Implémenter exception handler API | 1h |
| L-01 | 🟢 Basse | Ajouter logging SLF4J | 2h |
| L-02 | 🟢 Basse | Évaluer rate limiting | 4h |
| L-03 | 🟢 Basse | Valider format country code | 30min |
| I-01 | ℹ️ Info | Ajouter annotations JSR-380 | 1h |
| I-02 | ℹ️ Info | Tests de sécurité | 4h |
| I-03 | ℹ️ Info | Documentation SECURITY.md | 2h |
| I-04 | ℹ️ Info | Intégration SAST CI/CD | 4h |

---

## 🔗 Références

- [OWASP Top 10 2021](https://owasp.org/Top10/)
- [CWE Top 25 2023](https://cwe.mitre.org/top25/archive/2023/2023_top25_list.html)
- [OWASP Java Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Java_Security_Cheat_Sheet.html)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)

---

**Prochaine revue recommandée**: Après implémentation de la couche API/Web  
**Contact**: Security Review Agent
