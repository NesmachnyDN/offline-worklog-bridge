# Architecture

## Context

The bridge solves an integration problem in which direct system-to-system connectivity is unavailable. An operator can reach the source system only in network context A and the destination only in network context B.

```text
+------------------+       +---------------------------+       +---------------------+
| Source system    |       | Offline Worklog Bridge    |       | Destination system  |
|                  |       |                           |       |                     |
| issues/worklogs  |-----> | SourceWorklogPort         |       | weekly timesheet     |
|                  |       |        |                  |       |                     |
+------------------+       |        v                  |       +----------^----------+
   network A only          | local snapshot            |                  |
                           |        |                  |                  |
                           | allocation + preview      |                  |
                           |        |                  |                  |
                           | DestinationTimesheetPort  |------------------+
                           +---------------------------+
                                      network B only
```

## Boundaries

- **Source adapter:** vendor-specific capture. It maps remote records into the canonical `CapturedWorklog` model.
- **Local staging:** persists snapshots so no simultaneous remote connectivity is required.
- **Domain policy:** classifies and allocates time without depending on a particular tracker or timesheet product.
- **Destination adapter:** publishes the normalized daily timesheet model.
- **Publish ledger:** records idempotency keys and destination references.
- **UI:** local review surface for capture, allocation preview and publish.

The demo adapters are intentionally synthetic/local. A production adapter can be replaced without changing the allocation domain.
