# Domain and accounting model

`CapturedWorklog` is the canonical source record. It carries a source identifier, issue key/title, date, duration and category.

Two accounting views coexist:

1. **Detailed engineering view** — delivery and non-delivery worklogs are preserved exactly as captured.
2. **Destination contractual view** — the complete day is represented through delivery tasks because the destination model does not expose the same non-delivery activity taxonomy.

The transformation creates `DailyAllocation` and then `TimesheetEntry`. The source snapshot remains immutable from the mapping perspective; derived destination values are stored separately.
