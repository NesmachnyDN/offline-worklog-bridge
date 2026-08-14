# Offline network boundary

The bridge is intentionally two-phase.

## Capture phase

While network context A is active, the source adapter downloads the selected worklog period and creates a local snapshot. The operator can then disconnect from the source VPN/network.

## Review phase

No remote system is required. Mapping, allocation, traceability comments and the destination preview are computed from the staged snapshot.

## Publish phase

After switching to network context B, the destination adapter publishes the prepared entries. A local publish ledger prevents accidental duplicate submission when a publish operation is repeated.

This is a store-and-forward integration pattern, not a real-time distributed transaction.
