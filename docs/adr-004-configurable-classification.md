# ADR-004: Make source activity classification explicit and configurable

**Decision:** source adapters first map vendor records to `RawWorklog`; a separate classification policy decides whether an activity is `DELIVERY` or `NON_DELIVERY`.

The reference implementation uses configurable case-insensitive keywords. This is intentionally simple but keeps the business rule visible and testable.

**Why:** classification is accounting semantics, not a property of a particular REST DTO. Keeping it outside vendor client code allows the same mapping policy to survive a tracker migration or be replaced by richer project-specific rules.
