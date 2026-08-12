# LifeLink Synthetic Demo Dataset — Karnataka Statewide Edition

A ready-to-use, realistic-but-fake dataset covering Karnataka state (weighted so Bangalore dominates, as it does in reality), sized for a live demo/evaluation — not so small it looks empty, not so large it's unwieldy.

**Important:** the matching engine itself has no concept of "a city" or "a state" — it works on raw GPS distance and would function identically anywhere in the world. This dataset is Karnataka-shaped only because that's what was seeded; scaling to all of India later just means adding more cities to the `CITY_HUBS` list in the script and re-running it, no application code changes needed.

## What's inside
| File | Contents |
|---|---|
| `donors.csv` | 600 donors — name, phone, email, blood type, GPS location, last donation date, active flag, reliability score — spread across 20 Karnataka cities/towns, population-weighted (Bangalore ≈ 30x the weight of a small town) |
| `hospitals.csv` | 14 real hospitals across Bangalore, Mysore, Hubli-Dharwad, Mangalore, Belagavi, Kalaburagi, Davanagere, Ballari, and Shivamogga, with approximate real coordinates |
| `requests.csv` | 120 emergency blood requests spread over the last 30 days, weighted so ~60% originate near a hospital (as real emergencies do) |
| `seed_dataset.sql` | The same data as ready-to-run `INSERT` statements matching your exact schema (`V1__init_schema.sql`) — just run it against your Postgres+PostGIS DB |
| `generate_dataset.py` | The script that made all of the above — editable if you want more/fewer records, a different state, or all-India coverage |

## Scaling this to all of India later
Open `generate_dataset.py` and add more entries to the `CITY_HUBS` list — each is just `("City Name", latitude, longitude, relative_population_weight)`. Add Mumbai, Delhi, Chennai, Kolkata, Hyderabad, etc. with weights roughly proportional to their real population, add a few hospital names pinned to hub indices in `HOSPITAL_NAMES`, and re-run the script. Nothing else changes — same schema, same SQL output, same matching engine.

## Why these numbers, not made up
- **Blood-type mix** is drawn from a real published study: Patidar & Dhiman, *"Distribution of ABO and Rh (D) Blood groups in India: A systematic review,"* ISBT Science Series, 2021 — a meta-analysis of 1.43 million Indian donor records (A 23.16%, B 34.10%, O 34.56%, AB 8.18%; Rh+ 94.13%, Rh– 5.87%). The generated 300 donors land within a couple percentage points of these real ratios (see the script's printed summary) — worth screenshotting for your report as evidence the dataset is grounded, not arbitrary.
- **Locations** are scattered around 10 real Tirupati-area localities (railway station, SVU, Renigunta, Chandragiri, etc.) with a distance-decay function so donors cluster more densely near the city centre and thin out toward the outskirts — mimicking real population density instead of a uniform random scatter that would put donors in the middle of nowhere.
- **Last-donation dates** are deliberately mixed: ~25% recently donated (still inside the 90-day cooldown, so your eligibility filter has something real to reject), ~35% long dormant, rest in between — this means your cooldown logic and radius-expansion logic will actually get exercised in a demo instead of every donor trivially being eligible.
- **Requests** favor hospital-proximate origins (60%) since real emergency demand clusters around hospitals, with a realistic urgency split (30% CRITICAL / 35% HIGH / 35% NORMAL) and component-type split (70% whole blood / 20% platelets / 10% plasma, reflecting real-world transfusion demand patterns).

## How to use it
1. **Fastest path:** run `psql -U lifelink -d lifelink_db -f seed_dataset.sql` after your Flyway migrations have run (or drop it in as a new Flyway migration, e.g. `V13__demo_seed.sql`).
2. **To regenerate with different size/city:** edit `N_DONORS`, `N_REQUESTS`, or the `CITY_HUBS` list at the top of `generate_dataset.py` and re-run `python3 generate_dataset.py`.
3. **For the evaluation harness** we discussed earlier (match-rate by radius tier, Blood Chain activation rate): this dataset is exactly what that script should run against — it's deliberately sized so some requests will succeed at 5km, some will need the full 15/30km expansion, and — if you want to specifically test Blood Chain — you can trim donor density in one corner of `CITY_HUBS` (e.g. remove "Yerpedu" donors) to guarantee a few zero-donor-found cases.

## Honesty note
The Karnataka city coordinates are approximate central points for well-known cities/towns based on general geographic knowledge, not precision-geocoded addresses — good enough to make a demo/map look and behave realistically across the whole state, but don't present them as surveyed data. Likewise, the population weights used to spread donors across cities are rough proportional estimates, not census figures — if you want to cite exact numbers in your report, pull real 2011/2021 Census or municipal population figures for these cities and swap them into the `CITY_HUBS` weights. The blood-type distribution, by contrast, is a real, citable number from the paper above.
