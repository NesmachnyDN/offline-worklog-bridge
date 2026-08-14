# Adapter contracts and anti-corruption layer

The public implementation deliberately separates remote product semantics from the bridge domain.

## Source side

A vendor-specific source adapter is responsible for:

1. authenticating to the remote issue/worklog product;
2. reading remote issue and worklog records;
3. mapping the vendor payload into `RawWorklog`;
4. letting `SourceWorklogMapper` classify the activity and create the canonical `CapturedWorklog`.

The bridge domain never depends on a vendor issue, worklog-plugin object or REST DTO.

## Destination side

A destination adapter receives a canonical `TimesheetEntry`: date, total duration, traceability comment and idempotency key. It owns any proprietary weekly-grid API, authentication mechanism and remote response mapping.

This boundary is intentionally asymmetric: source accounting is detailed, while the destination receives a derived contractual view.
