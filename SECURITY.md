# Security

The demo deliberately runs only on loopback and stores runtime data locally. No credentials are required for the synthetic adapters.

Future real adapters must obtain credentials from local secret storage or environment configuration; credentials, access tokens, production payloads and corporate endpoints must never be committed.

The CI public-safety scan rejects common credential patterns, private keys, private-network addresses and historical organization markers.
