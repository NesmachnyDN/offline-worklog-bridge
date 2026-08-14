# Offline Worklog Bridge

An offline-first bridge for reconciling and publishing worklogs when the source tracker and destination timesheet are reachable only from **mutually exclusive network contexts**.

The project demonstrates a store-and-forward integration pattern: capture detailed worklogs while connected to the source environment, persist an immutable local snapshot, disconnect, review the accounting transformation, then publish the prepared timesheet after switching to the destination environment.

## Architecture problem

```text
NETWORK CONTEXT A                         LOCAL BRIDGE                         NETWORK CONTEXT B
Source issue/worklog system   -->   capture + staging + mapping   -->   Destination timesheet
        reachable only here               persists locally                   reachable only here

                 <---------- the two remote systems are never required simultaneously ---------->
```

The historical business problem had another mismatch: the source system tracked delivery work and team overhead (planning, meetings, retrospectives, coordination) separately, while the destination accounting model expected the full paid day to be represented through delivery work. The bridge therefore needs an explicit mapping policy rather than a blind field-to-field copy.

## Implemented workflow

1. **Capture** detailed source worklogs into an embedded local snapshot.
2. **Classify** vendor-neutral raw worklogs as `DELIVERY` or `NON_DELIVERY` using an explicit, configurable policy.
3. **Reconcile workload** against a standard 8-hour weekday / 40-hour work-week expectation and surface under/over-reporting without changing source data.
4. **Allocate** non-delivery time proportionally across delivery tasks for the same day.
5. **Preview** exactly what the destination weekly timesheet will receive.
6. **Publish** one aggregated destination entry per day with traceable issue references in the comment.
7. **Prevent duplicates** with a stable SHA-256 idempotency key and a local publish ledger.

The public demo uses synthetic source data and a local destination adapter. Vendor-specific APIs are ports/adapters, not part of the domain model. Source payloads cross an explicit anti-corruption layer (`RawWorklog` → classification → `CapturedWorklog`).

## Why proportional allocation

The destination model does not expose the same activity taxonomy as the source. For a day such as:

```text
DEV-101 delivery       3h
DEV-102 delivery       2h
planning               1h
team meeting           1h
retrospective          1h
                       --
                       8h
```

three hours of non-delivery time are allocated according to the relative delivery effort:

```text
DEV-101 destination    4h 48m
DEV-102 destination    3h 12m
                       ------
                       8h 00m
```

The algorithm uses a largest-remainder step so integer-minute rounding always conserves the exact daily total. A day containing only non-delivery activity is **blocked for review** instead of inventing a target task.

## Traceability

Source worklogs are never overwritten. The local snapshot preserves the detailed accounting view, while the destination comment records the delivery issues and the amount of allocated overhead. This lets the two systems retain different accounting semantics without losing provenance.

## Technology baseline

- Java 21
- Spring Boot 4.1
- Vaadin 25.2
- Spring Data JPA
- embedded H2
- Maven

The local UI binds to `127.0.0.1` only.

## Run

Development requirements: Java 21+, Maven 3.8+, Node.js 24+ for a production Vaadin build.

```bash
mvn spring-boot:run
```

Open `http://127.0.0.1:8080` and click **Capture synthetic work week**. The application persists its staging database under `data/`, so the captured snapshot survives a network/VPN switch and an application restart.

## Architecture documentation

- [Architecture](docs/architecture.md)
- [Domain and accounting model](docs/domain-model.md)
- [Allocation policy](docs/allocation-policy.md)
- [Workload reconciliation](docs/reconciliation.md)
- [Adapter contracts / anti-corruption layer](docs/adapter-contracts.md)
- [Offline network boundary](docs/offline-network-boundary.md)
- [ADR-001: Ports and adapters](docs/adr-001-ports-and-adapters.md)
- [ADR-002: Local staging / store-and-forward](docs/adr-002-local-staging.md)
- [ADR-003: Proportional allocation](docs/adr-003-proportional-allocation.md)
- [ADR-004: Configurable activity classification](docs/adr-004-configurable-classification.md)

## Roadmap

- Current: synthetic source adapter, configurable classification, persistent snapshots, workload reconciliation, proportional allocation, preview, local destination adapter and idempotent publishing.
- Next: optional sanitized Jira-compatible source adapter example.
- Next: generic REST destination adapter with explicit dry-run/reconciliation output.
- Optional: import/export snapshot package for physically separated machines.

## Development approach

**Role:** architecture, original problem framing, business mapping rules, integration model, implementation direction and engineering review.

**Development approach:** AI-assisted reimplementation and modernization with human architecture ownership and review.

See [ORIGIN.md](ORIGIN.md) for the publication boundary.
