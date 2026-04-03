---
name: carbon-anomaly-detection
description: 'Detect anomalous carbon or energy usage patterns in EcoTrack tenant-scoped data. Use for baseline drift detection, daily spike analysis, and actionable investigation guidance with deterministic checks.'
argument-hint: 'Provide tenantId, dateRange, granularity, and ask for a quick checklist output.'
user-invocable: true
disable-model-invocation: false
---

# Carbon Anomaly Detection Skill

## What This Skill Produces
- A fast tenant-scoped anomaly triage in checklist format.
- Top suspicious spikes with severity and confidence.
- Immediate actions to assign within operations, data, or infrastructure.

## When To Use
- Sudden daily or weekly jumps in kWh or gCO2e appear.
- You need a first-pass answer in less than 10 minutes.
- A tenant asks if a spike is expected or problematic.

## Required Inputs
- tenantId (mandatory)
- dateRange (mandatory)
- granularity (daily or hourly)
- optional: assetType, energySource, knownEvents

## Quick Checklist
1. Scope lock.
- Confirm one tenantId only.
- Confirm unit consistency (kWh and gCO2e).

2. Data sanity.
- Ensure required period data exists.
- If missing data > 10%, mark low-confidence.

3. Baseline quick build.
- Use same-tenant historical window.
- Prefer median and MAD.

4. Spike detection.
- Flag robust z-score > 3.5.
- If sparse baseline, flag day-over-day change > 30%.

5. Cause triage.
- Match with knownEvents first.
- If isolated to one group: localized issue.
- If broad across groups: systemic issue.

6. Conversion integrity check.
- Verify kWh to gCO2e factor for tenant and date.
- Confirm no factor version drift.

7. Action output.
- Return top 3 anomalies only.
- Include severity, confidence, likelyCause, recommendedAction.

## Decision Rules
- high-severity: > 50% above baseline or persistent 3+ periods.
- medium-severity: 20% to 50% above baseline.
- low-severity: < 20% above baseline and event-aligned.
- high-confidence: complete data and stable factors.
- low-confidence: missing data, mixed granularity, or recent factor updates.

## Completion Checks
- Tenant isolation validated.
- Unit consistency validated.
- Detection thresholds applied.
- Known events compared.
- Conversion-factor integrity checked.
- Output contains top 3 anomalies and actions.

## Output Template
~~~json
{
  "tenantId": "string",
  "dateRange": { "from": "YYYY-MM-DD", "to": "YYYY-MM-DD" },
  "topAnomalies": [
    {
      "timestamp": "ISO-8601",
      "metric": "kWh|gCO2e",
      "deltaPct": 0.0,
      "severity": "low|medium|high",
      "confidence": "low|high",
      "likelyCause": "string",
      "recommendedAction": "string"
    }
  ],
  "summary": {
    "systemicSignal": false,
    "requiresDataBackfill": false
  }
}
~~~

## Usage Prompts
- /carbon-anomaly-detection Run a quick checklist for tenant T-042 over the last 30 days and return top 3 anomalies.
- /carbon-anomaly-detection Quick triage for hourly spikes this week, high-confidence items only.
- /carbon-anomaly-detection Checklist mode: verify whether the March gCO2e spike is due to factor drift or real usage.

## Notes For EcoTrack
- Keep all queries and aggregations tenant-scoped.
- Do not infer across tenants.
- Preserve deterministic behavior and reproducible thresholds.
