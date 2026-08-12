"""
LifeLink synthetic dataset generator -- Karnataka statewide edition
---------------------------------------------------------------------
Generates a realistic (but fake) dataset of donors, hospitals, and emergency
requests spread across Karnataka state (Bangalore-weighted, since it holds
roughly a third of the state's population), matching LifeLink's actual DB
schema (V1__init_schema.sql: users, donors, hospitals, emergency_requests).

This is intentionally NOT city-limited -- the matching engine itself has no
concept of a city boundary (see accompanying explanation); this dataset is
just sized to look like a real state instead of a real city. The same script
scales to all of India by adding more hub cities to CITY_HUBS.

Blood-type distribution is based on a real published national study:
Patidar & Dhiman, "Distribution of ABO and Rh (D) Blood groups in India:
A systematic review," ISBT Science Series, 2021 (1,429,996 donor records
across 23 Indian states) -> A 23.16%, B 34.10%, O 34.56%, AB 8.18%,
Rh+ 94.13%, Rh- 5.87%. Combined into 8 ABO x Rh types below.

Output:
  donors.csv, hospitals.csv, requests.csv   (human-readable)
  seed_dataset.sql                          (ready to run against the DB)
"""

import csv
import json
import math
import random
import uuid
from datetime import datetime, timedelta

from faker import Faker

fake = Faker("en_IN")
random.seed(42)
Faker.seed(42)

# ---------------------------------------------------------------------------
# 1. Real blood-type distribution (India, Patidar & Dhiman 2021)
# ---------------------------------------------------------------------------
ABO = {"A": 0.2316, "B": 0.3410, "O": 0.3456, "AB": 0.0818}
RH = {"+": 0.9413, "-": 0.0587}
BLOOD_TYPE_WEIGHTS = {}
for abo, abo_p in ABO.items():
    for rh, rh_p in RH.items():
        BLOOD_TYPE_WEIGHTS[f"{abo}{rh}"] = abo_p * rh_p
BLOOD_TYPES = list(BLOOD_TYPE_WEIGHTS.keys())
BLOOD_WEIGHTS = list(BLOOD_TYPE_WEIGHTS.values())

# ---------------------------------------------------------------------------
# 2. Real-ish reference points across Karnataka
#    (approximate central coordinates of major cities/towns, used only as
#    realistic anchor points to cluster donors around -- not precision
#    geocoding. Weights are a rough proxy for relative real population,
#    e.g. Bangalore >> Mysore/Hubli-Dharwad/Mangalore >> smaller towns,
#    so the generated donor density looks like a real state, not uniform.)
# ---------------------------------------------------------------------------
CITY_HUBS = [
    # (name, lat, lon, relative population weight)
    ("Bangalore (Bengaluru)", 12.9716, 77.5946, 30),
    ("Mysore (Mysuru)", 12.2958, 76.6394, 6),
    ("Hubli-Dharwad", 15.3647, 75.1240, 5),
    ("Mangalore (Mangaluru)", 12.9141, 74.8560, 5),
    ("Belgaum (Belagavi)", 15.8497, 74.4977, 4),
    ("Kalaburagi (Gulbarga)", 17.3297, 76.8343, 4),
    ("Davanagere", 14.4644, 75.9218, 3),
    ("Ballari (Bellary)", 15.1394, 76.9214, 3),
    ("Vijayapura (Bijapur)", 16.8302, 75.7100, 2),
    ("Shivamogga (Shimoga)", 13.9299, 75.5681, 2),
    ("Tumakuru (Tumkur)", 13.3379, 77.1173, 2),
    ("Udupi", 13.3409, 74.7421, 2),
    ("Hassan", 13.0068, 76.1004, 1),
    ("Mandya", 12.5242, 76.8958, 1),
    ("Chikmagalur", 13.3161, 75.7720, 1),
    ("Bagalkot", 16.1691, 75.6640, 1),
    ("Raichur", 16.2076, 77.3463, 1),
    ("Bidar", 17.9104, 77.5199, 1),
    ("Kolar", 13.1362, 78.1298, 1),
    ("Chitradurga", 14.2226, 76.3985, 1),
]
HUB_WEIGHTS = [h[3] for h in CITY_HUBS]

HOSPITAL_NAMES = [
    ("Victoria Hospital, Bangalore", 0),
    ("NIMHANS, Bangalore", 0),
    ("St. John's Medical College Hospital, Bangalore", 0),
    ("Manipal Hospital, Bangalore", 0),
    ("K.C. General Hospital, Bangalore", 0),
    ("Karnataka State Blood Transfusion Council Bank, Bangalore", 0),
    ("K.R. Hospital, Mysore", 1),
    ("Karnataka Institute of Medical Sciences (KIMS), Hubli", 2),
    ("Government Wenlock Hospital, Mangalore", 3),
    ("Belagavi Institute of Medical Sciences (BIMS)", 4),
    ("Kalaburagi Institute of Medical Sciences", 5),
    ("Chigateri District Hospital, Davanagere", 6),
    ("Vijayanagara Institute of Medical Sciences, Ballari", 7),
    ("SDM Medical College Hospital, Shivamogga area", 9),
]

COMPONENT_TYPES = ["WHOLE_BLOOD", "PLATELETS", "PLASMA"]
COMPONENT_WEIGHTS = [0.7, 0.2, 0.1]  # whole blood dominates real demand
URGENCIES = ["CRITICAL", "HIGH", "NORMAL"]
URGENCY_WEIGHTS = [0.3, 0.35, 0.35]

N_DONORS = 600
N_HOSPITALS = len(HOSPITAL_NAMES)
N_REQUESTS = 120


def jittered_point(lat, lon, max_km=4.0):
    """Random point within max_km of (lat, lon), denser near the centre."""
    r_km = (random.random() ** 1.8) * max_km  # bias towards the centre
    theta = random.uniform(0, 2 * math.pi)
    dlat = (r_km / 111.0) * math.cos(theta)
    dlon = (r_km / (111.0 * math.cos(math.radians(lat)))) * math.sin(theta)
    return round(lat + dlat, 6), round(lon + dlon, 6)


def pick_blood_type():
    return random.choices(BLOOD_TYPES, weights=BLOOD_WEIGHTS, k=1)[0]


def sample_last_donation_date():
    """
    Realistic mix: ~35% never donated / long dormant (>180d, fully eligible),
    ~25% recently donated (within 90d, likely still in cooldown),
    ~40% spread between.
    """
    bucket = random.random()
    if bucket < 0.35:
        days_ago = random.randint(180, 900)
    elif bucket < 0.60:
        days_ago = random.randint(1, 89)
    else:
        days_ago = random.randint(90, 179)
    return (datetime.now() - timedelta(days=days_ago)).date()


# ---------------------------------------------------------------------------
# Generate hospitals (each pinned near its named city's hub)
# ---------------------------------------------------------------------------
hospitals = []
for name, hub_idx in HOSPITAL_NAMES:
    hub = CITY_HUBS[hub_idx]
    lat, lon = jittered_point(hub[1], hub[2], max_km=2.0)
    hospitals.append({
        "id": str(uuid.uuid4()),
        "name": name,
        "lat": lat,
        "lon": lon,
        "verified": True,
    })

# ---------------------------------------------------------------------------
# Generate donors (+ their user records)
# ---------------------------------------------------------------------------
donors = []
for i in range(N_DONORS):
    hub = random.choices(CITY_HUBS, weights=HUB_WEIGHTS, k=1)[0]
    # bigger cities get a wider spread radius (more suburbs/sprawl)
    spread = 12.0 if hub[3] >= 20 else (7.0 if hub[3] >= 4 else 4.0)
    lat, lon = jittered_point(hub[1], hub[2], max_km=spread)
    donors.append({
        "user_id": str(uuid.uuid4()),
        "donor_id": str(uuid.uuid4()),
        "name": fake.name(),
        "phone": "+91" + fake.msisdn()[3:],
        "email": fake.unique.email(),
        "blood_type": pick_blood_type(),
        "lat": lat,
        "lon": lon,
        "last_donation_date": sample_last_donation_date(),
        "is_active": random.random() > 0.08,  # ~8% toggled unavailable
        "reliability_score": round(random.uniform(70, 100), 2),
    })

# ---------------------------------------------------------------------------
# Generate emergency requests (+ their requester user records)
# ---------------------------------------------------------------------------
requests = []
for i in range(N_REQUESTS):
    # weight requests near hospitals, since real emergencies cluster there
    if random.random() < 0.6:
        h = random.choice(hospitals)
        lat, lon = jittered_point(h["lat"], h["lon"], max_km=1.5)
    else:
        hub = random.choice(CITY_HUBS)
        lat, lon = jittered_point(hub[1], hub[2], max_km=5.0)

    requests.append({
        "requester_user_id": str(uuid.uuid4()),
        "requester_name": fake.name(),
        "requester_phone": "+91" + fake.msisdn()[3:],
        "request_id": str(uuid.uuid4()),
        "blood_type": pick_blood_type(),
        "component_type": random.choices(COMPONENT_TYPES, weights=COMPONENT_WEIGHTS, k=1)[0],
        "urgency": random.choices(URGENCIES, weights=URGENCY_WEIGHTS, k=1)[0],
        "lat": lat,
        "lon": lon,
        "created_offset_hours": random.randint(0, 720),  # spread over last 30 days
    })

# ---------------------------------------------------------------------------
# Write CSVs
# ---------------------------------------------------------------------------
with open("hospitals.csv", "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=["id", "name", "lat", "lon", "verified"])
    w.writeheader()
    w.writerows(hospitals)

with open("donors.csv", "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=list(donors[0].keys()))
    w.writeheader()
    w.writerows(donors)

with open("requests.csv", "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=list(requests[0].keys()))
    w.writeheader()
    w.writerows(requests)

# ---------------------------------------------------------------------------
# Write ready-to-run SQL matching V1__init_schema.sql
# ---------------------------------------------------------------------------
sql_lines = ["-- LifeLink synthetic seed dataset", "-- Generated for demo/evaluation purposes\n"]

sql_lines.append("-- Hospitals")
for h in hospitals:
    sql_lines.append(
        f"INSERT INTO hospitals (id, name, location, verified) VALUES "
        f"('{h['id']}', '{h['name'].replace(chr(39), chr(39)+chr(39))}', "
        f"ST_SetSRID(ST_MakePoint({h['lon']}, {h['lat']}), 4326)::geography, {str(h['verified']).lower()});"
    )

sql_lines.append("\n-- Donors (users + donors)")
for d in donors:
    name_esc = d["name"].replace("'", "''")
    sql_lines.append(
        f"INSERT INTO users (id, name, phone, email, password_hash, role) VALUES "
        f"('{d['user_id']}', '{name_esc}', '{d['phone']}', '{d['email']}', "
        f"'$2a$10$demoHashPlaceholderDoNotUseInProd', 'DONOR');"
    )
    sql_lines.append(
        f"INSERT INTO donors (id, user_id, blood_type, location, last_donation_date, "
        f"reliability_score, is_active) VALUES "
        f"('{d['donor_id']}', '{d['user_id']}', '{d['blood_type']}', "
        f"ST_SetSRID(ST_MakePoint({d['lon']}, {d['lat']}), 4326)::geography, "
        f"'{d['last_donation_date']}', {d['reliability_score']}, {str(d['is_active']).lower()});"
    )

sql_lines.append("\n-- Requesters + emergency requests")
for r in requests:
    name_esc = r["requester_name"].replace("'", "''")
    sql_lines.append(
        f"INSERT INTO users (id, name, phone, password_hash, role) VALUES "
        f"('{r['requester_user_id']}', '{name_esc}', '{r['requester_phone']}', "
        f"'$2a$10$demoHashPlaceholderDoNotUseInProd', 'REQUESTER');"
    )
    sql_lines.append(
        f"INSERT INTO emergency_requests (id, requester_id, blood_type, component_type, urgency, "
        f"location, status, current_radius_km, created_at, expires_at) VALUES "
        f"('{r['request_id']}', '{r['requester_user_id']}', '{r['blood_type']}', "
        f"'{r['component_type']}', '{r['urgency']}', "
        f"ST_SetSRID(ST_MakePoint({r['lon']}, {r['lat']}), 4326)::geography, "
        f"'PENDING', 5, "
        f"now() - interval '{r['created_offset_hours']} hours', "
        f"now() - interval '{r['created_offset_hours']} hours' + interval '24 hours');"
    )

with open("seed_dataset.sql", "w") as f:
    f.write("\n".join(sql_lines) + "\n")

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
print(f"Generated {len(donors)} donors, {len(hospitals)} hospitals, {len(requests)} requests")
bt_counts = {}
for d in donors:
    bt_counts[d["blood_type"]] = bt_counts.get(d["blood_type"], 0) + 1
print("Blood type distribution in generated donors:")
for bt in BLOOD_TYPES:
    pct = 100 * bt_counts.get(bt, 0) / len(donors)
    print(f"  {bt:>3}: {bt_counts.get(bt, 0):4d}  ({pct:5.1f}%)")
