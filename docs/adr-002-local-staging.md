# ADR-002: Use persistent local staging

**Decision:** capture worklogs into an embedded local database before publishing.

**Drivers:**
- source and destination are reachable from mutually exclusive network contexts;
- the operator must be able to disconnect/reconnect without losing the source dataset;
- transformation and review should not depend on either remote system;
- retries must not require a second source download.

**Trade-off:** local data becomes an asset that needs lifecycle and confidentiality controls in a production deployment.
