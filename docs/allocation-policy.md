# Proportional allocation policy

For each day, let delivery task durations be `d1..dn`, delivery total `D`, and non-delivery total `N`.

Each task receives an exact additional share:

`share_i = N * di / D`

Durations are stored as integer minutes, so the implementation first takes the floor of each share and then assigns the remaining minutes by descending fractional remainder. This is the largest-remainder method and guarantees:

- no minute is created or lost;
- larger delivery contributions receive proportionally larger overhead;
- rounding is deterministic;
- the destination daily total equals the captured source daily total.

If `D = 0`, publication is blocked. The bridge does not invent a delivery target or move time to a different day.
