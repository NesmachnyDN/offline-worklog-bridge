# ADR-001: Isolate source and destination products behind ports

**Decision:** the domain depends on `SourceWorklogPort` and `DestinationTimesheetPort`, not vendor client libraries.

**Why:** the original problem involved different products and a non-standard destination accounting model. Product APIs can change independently, while capture, allocation, preview and idempotency should remain stable.

**Consequence:** adapters own vendor payload mapping; the domain model stays portable and testable with synthetic data.
