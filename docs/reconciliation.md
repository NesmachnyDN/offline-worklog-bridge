# Workload reconciliation

The bridge keeps accounting transformation separate from completeness checks.

The reference policy expects 480 minutes for Monday-Friday and zero for weekends. Every captured date is compared against that expectation and classified as `OK`, `UNDER_REPORTED` or `OVER_REPORTED`.

Reconciliation is intentionally **non-destructive**: it never manufactures missing time, removes overtime or changes a source worklog. It is a review signal for the operator before mapping/publish. A production deployment can replace the reference calendar with an organizational calendar that knows holidays, part-time schedules and leave.
