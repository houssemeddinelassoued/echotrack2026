# Copilot Instructions - EcoTrack

## Project Context
EcoTrack is a B2B SaaS platform that helps companies calculate, analyze, and reduce their digital carbon footprint.

Core MVP capabilities:
- IT asset inventory management
- Daily energy consumption tracking
- Carbon conversion from kWh to gCO2e
- Strict multi-tenant data isolation

## Technical Constraints
- Use Java 21 only.
- Prefer clear, maintainable, production-ready code.
- Keep architecture and implementations compatible with a multi-tenant SaaS model.

## Coding Standards
- Apply SOLID principles.
- Prefer composition over inheritance.
- Use explicit and meaningful English names for variables, methods, and classes.
- Keep methods small and focused on a single responsibility.
- Avoid duplicated logic; extract reusable components.

## Documentation Rules
- Add standard Javadoc for all public methods.
- Javadoc must describe purpose, parameters, return values, and thrown exceptions.

## Error Handling
- Use custom exceptions for business and domain errors.
- Do not swallow exceptions silently.
- Surface actionable error messages for troubleshooting.

## Performance and Complexity
- Optimize implementations to reduce algorithmic complexity (Big O).
- Avoid unnecessary nested loops and repeated scans over large collections.
- Prefer streaming/aggregation strategies that are efficient and readable.

## Domain Rules
- Keep tenant boundaries explicit in services, repositories, and queries.
- Never mix data between tenants.
- Preserve consistency between energy values (kWh) and carbon values (gCO2e).
- Ensure business logic remains deterministic and testable.

## Testing Expectations
- Add or update tests for non-trivial behavior changes.
- Cover normal flows, edge cases, and failure paths.
- Validate tenant isolation behavior in tests when relevant.
