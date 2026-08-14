# ADR-003: Allocate non-delivery time proportionally to delivery tasks

**Decision:** when the destination accounting taxonomy cannot represent team overhead, distribute that time across delivery tasks from the same day in proportion to their source durations.

**Rejected alternatives:**
- dropping non-delivery time would under-report the paid day;
- assigning all overhead to one task would distort relative effort;
- moving overhead to another day would break temporal traceability;
- silently inventing a delivery task is not acceptable.

The source records remain intact. Allocation is a derived destination view and is visible before publish.
