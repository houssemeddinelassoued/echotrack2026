---
name: spring-boot-controller
description: Crée des Controllers Spring Boot REST conformes aux standards EcoTrack - multi-tenant, validation, DTOs, exception handling, et documentation OpenAPI.
argument-hint: <entity-name> [options: --crud | --readonly | --custom]
---

# Création de Controller Spring Boot

**Description:** Utilise ce skill chaque fois que l'utilisateur demande de créer un nouveau Controller REST, un endpoint API ou une route dans notre application Java Spring Boot.

## Instructions obligatoires
Lorsque tu génères un Controller REST pour ce projet, tu DOIS scrupuleusement respecter les règles d'architecture suivantes :

1. **Injection de dépendances :** N'utilise jamais `@Autowired`. Utilise toujours l'injection par constructeur via l'annotation Lombok `@RequiredArgsConstructor`.
2. **Format de réponse :** Tous les endpoints doivent renvoyer un objet `ResponseEntity<CustomResponse<T>>` (la classe CustomResponse existe déjà dans `com.entreprise.api.dto`).
3. **Documentation :** Chaque méthode doit obligatoirement avoir les annotations OpenAPI `@Operation` (avec summary et description) et `@ApiResponses`.
4. **Nommage :** Les URLs doivent être en minuscules, au pluriel, et préfixées par `/api/v1/` (ex: `/api/v1/customers`).

## Exemple de structure attendue :
```java
@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@Tag(name = "Entity", description = "API pour la gestion des entités")
public class EntityController {

    private final EntityService entityService;

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une entité", description = "Retourne les détails d'une entité via son ID")
    public ResponseEntity<CustomResponse<EntityDto>> getEntity(@PathVariable Long id) {
        // Implémentation
    }
}