#!/usr/bin/env bash
set -euo pipefail
fail=0
scan() {
  local label="$1" pattern="$2"
  if grep -RInE --exclude-dir=.git --exclude='check-public-safety.sh' "$pattern" .; then
    echo "Public-safety failure: $label" >&2
    fail=1
  fi
}
scan "private key material" 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY'
scan "credential-looking URL" 'https?://[^[:space:]/:@]+:[^[:space:]@]+@'
scan "GitHub token" 'gh[pousr]_[A-Za-z0-9_]{20,}'
scan "common cloud access key" 'AKIA[0-9A-Z]{16}'
scan "corporate-only DNS suffix" '\.(corp|internal|intranet)([./:]|$)'
scan "RFC1918 IPv4 address" '(^|[^0-9])(10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}|192\.168\.[0-9]{1,3}\.[0-9]{1,3}|172\.(1[6-9]|2[0-9]|3[01])\.[0-9]{1,3}\.[0-9]{1,3})([^0-9]|$)'
scan "historical organization markers" '(VTB Group|Ланит|LANIT|vtb\.gos|task\.corp)'
scan "obvious committed password" '(password|passwd|secret)[[:space:]]*[:=][[:space:]]*[^$<{[:space:]][^[:space:]]{3,}'
exit "$fail"
