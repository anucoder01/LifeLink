# LifeLink 🩸 — Blood Donor & Emergency Request Network

![CI](https://github.com/anucoder01/LifeLink/actions/workflows/ci.yml/badge.svg)
> **Real-time, geo-targeted blood donor matching with social impact features designed to save lives where conventional systems fail.**

LifeLink replaces manual WhatsApp-group blood requests with a reliable, privacy-first matching system powered by PostGIS geo-queries, Firebase push notifications, and a unique **Blood Chain** social vouching network.

---

## ✨ Features

### Core Platform
- 🔐 **JWT Authentication** — secure stateless auth with role-based access (`DONOR`, `REQUESTER`, `HOSPITAL_ADMIN`)
- 📍 **Geo-targeted Matching** — PostGIS `ST_DWithin` finds eligible donors within radius; auto-expands 5km → 15km → 30km if unanswered
- 🩸 **Blood Type Compatibility Matrix** — full ABO/Rh compatibility rules enforced at matching time
- ⏱️ **Donation Eligibility Cooldowns** — Whole Blood (90d), Platelets (14d), Plasma (28d)
- 🔔 **Firebase Push Notifications (FCM)** — instant alerts to matched donors with priority flag for CRITICAL urgency
- 📵 **Donor Respond Flow** — donors explicitly ACCEPT or DECLINE; request promoted to IN_PROGRESS on first acceptance
- 📵 **Active Status Toggle** — donors can mark themselves unavailable; excluded from all matching
- ⏰ **Scheduled Jobs** — auto radius-expansion every 5 min, auto-expiry check every 60s
- 🏥 **Hospital & Blood Inventory** — data model for hospital blood stock tracking

### Frontend Application (Vite + React)
- ⚛️ **Modern React Framework** — lightning-fast Vite + React stack with 0-config HMR
- 🎨 **Glassmorphism UI** — state-of-the-art vanilla CSS system without relying on Tailwind
- 🗺️ **Real-time Geolocation** — integrates browser HTML5 Geolocation API to auto-fetch exact GPS coordinates
- ☎️ **International Dialing** — `react-phone-number-input` integration for global phone number support
- 🚑 **My SOS Requests** — Track live donor responses, request volunteer drivers, and close requests
- 🏥 **Hospital Admin Portal** — Manage realtime webhooks and visualize blood bank inventory
- 🚙 **Driver Dashboard** — Volunteer logistics platform for blood bag delivery and donor transport
- 🤝 **NGO Partner Portal** — Analytics dashboard for NGOs to track campaign impact and donor turnout
- 🏕️ **Blood Donation Camps** — Interface to discover and register for local blood donation drives

### Feature A — Blood Chain 🔗 *(Social Vouching Network)*
> *When no registered donors are found within 30km, the Blood Chain activates.*

Each donor nominates up to **3 trusted contacts** as backup donors. When the matching engine exhausts the 30km radius with zero eligible donors:

1. The system generates a unique **72-hour one-time invite link** per contact
2. An **SMS** is sent to each vouched contact: *"Your friend said you'd help. O+ blood is needed nearby. Register here: [link]"*
3. The contact taps the link → registration form pre-fills their name & phone → they register as a donor in ~2 minutes
4. The token is consumed on use; expired tokens are cleaned up daily

**Privacy guarantees:** Contact data is held only until they opt in. Phone numbers are masked in all API responses. Donors can remove contacts at any time.

**SMS:** Powered by a Twilio-compatible stub (logs in dev mode). Set `TWILIO_ENABLED=true` to send real SMS.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.x |
| **Database** | PostgreSQL 15+ with PostGIS extension |
| **Spatial Queries** | Hibernate Spatial + PostGIS `ST_DWithin` |
| **Caching** | Redis 7.x |
| **Auth** | JWT (jjwt 0.12.3) |
| **Push Notifications** | Firebase Cloud Messaging (FCM) via Firebase Admin SDK |
| **SMS** | Twilio (stub included, activate with env vars) |
| **Migrations** | Flyway |
| **API Docs** | OpenAPI 3 / Swagger UI |
| **Containerization** | Docker Compose |

---

## 🚀 Quick Start

### Prerequisites
- Java 21 ([Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21))
- Docker & Docker Compose

### 1. Configure Environment Variables
```bash
cp .env.example .env
```
Edit `.env` with your database credentials, JWT secret, Firebase config path, and (optionally) Twilio credentials.

### 2. Start Infrastructure
```bash
docker-compose up -d postgres redis
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```
Flyway migrations run automatically and seed the database with sample data.

### 4. Explore the API
Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 📡 Key API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login, returns JWT |
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/register/invited?token=` | Register via Blood Chain invite link |

### Donor
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/donors/me` | Get my donor profile |
| `PUT` | `/api/v1/donors/me/location` | Update GPS location |
| `PUT` | `/api/v1/donors/me/fcm-token` | Register/refresh FCM push token |
| `PUT` | `/api/v1/donors/me/active?active=` | Toggle availability |

### Emergency Requests
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/requests` | Create emergency blood request |
| `POST` | `/api/v1/requests/{id}/respond` | ACCEPT or DECLINE a request |
| `PUT` | `/api/v1/requests/{id}/fulfill` | Mark request as fulfilled |
| `PUT` | `/api/v1/requests/{id}/cancel` | Cancel a request |
| `GET` | `/api/v1/requests/{id}` | Get request details |

### Blood Chain
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/donors/me/vouches` | 🔒 | List trusted backup contacts |
| `POST` | `/api/v1/donors/me/vouches` | 🔒 | Add a trusted contact (max 3) |
| `DELETE` | `/api/v1/donors/me/vouches/{id}` | 🔒 | Remove a trusted contact |
| `GET` | `/api/v1/blood-chain/invite/{token}` | 🌐 | Validate invite link (public) |

---

## 🏗 Architecture Overview

```
Emergency Request Created
        │
        ▼
  Matching Engine (PostGIS ST_DWithin)
  ┌─────────────────────────────────┐
  │  Radius: 5km → 15km → 30km     │
  │  Blood type compatibility check │
  │  Donation eligibility check     │
  └─────────────────────────────────┘
        │
        ├─ Donors found → FCM push notification
        │
        └─ No donors at 30km → BLOOD CHAIN ACTIVATES
                │
                ▼
         SMS to vouched contacts
         (one-time 72hr invite link)
                │
                ▼
         Contact registers → joins donor pool
```

### Key Design Decisions
- **Anonymity first**: Donor contact info is never exposed until they explicitly ACCEPT a request
- **Dual notification**: FCM for registered donors, SMS fallback for Blood Chain contacts
- **Privacy-preserving SMS**: Contact phone numbers are masked in all API responses
- **Scheduled expansion**: Radius expands every 5 min; Blood Chain only triggers at max radius with zero results

---

## 🗺 Roadmap

See [ROADMAP.md](./ROADMAP.md) for the full feature roadmap. 100+ features organized across 17 phases:

| Phase | Focus |
|---|---|
| **Phase 1** | Core backend completion (pagination, webhooks, tests) |
| **Phase 2** | Extended auth — blood bank, NGO, OTP, Google SSO, ABHA |
| **Phase 3** | Donor health profiling, questionnaire, consent management |
| **Phase 4** | Geo-matching extensions — ETA routing, live location, geo-fencing |
| **Phase 5** | Emergency system — smart ranking, cascading, SOS escalation, disaster mode |
| **Phase 6** | Smart notifications — SMS/WhatsApp fallback, silent hours, in-app center |
| **Phase 7** | Blood Chain extensions + Driver Network + Family Pacts + Communities |
| **Phase 8** | Gamification — impact dashboard, badges, XP, leaderboards, streaks |
| **Phase 9** | Hospital & blood bank platform — live tracking, analytics, cross-hospital |
| **Phase 10** | AI features — smart ranking, demand prediction, fake detection, chatbot |
| **Phase 11** | Trust & safety — rare blood registry, reputation, audit logs, rate limiting |
| **Phase 12** | Analytics — heatmaps, blood group stats, response times, KPIs |
| **Phase 13** | Post-donation health companion — 7-day recovery protocol |
| **Phase 14** | Accessibility, multilingual support, digital donor cards, certificates |
| **Phase 15** | Community — camps, disaster mode, corporate CSR, campus ambassadors |
| **Phase 16** | Infrastructure hardening — rate limiting, monitoring, backups, encryption |
| **Phase 17** | Frontend — React/Flutter Web donor dashboard, hospital admin, public portal |

---

## 🔒 Environment Variables

| Variable | Required | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | ✅ | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | DB password |
| `JWT_SECRET` | ✅ | HS256 secret (min 32 bytes) |
| `JWT_EXPIRATION` | ❌ | Token TTL in ms (default: 86400000) |
| `FIREBASE_CONFIG_PATH` | ✅ | Path to Firebase service account JSON |
| `APP_BASE_URL` | ❌ | Public URL for invite links (default: localhost:8080) |
| `TWILIO_ENABLED` | ❌ | Set `true` to send real SMS (default: false) |
| `TWILIO_SID` | ⚠️ | Twilio Account SID (required if enabled) |
| `TWILIO_AUTH_TOKEN` | ⚠️ | Twilio Auth Token (required if enabled) |
| `TWILIO_FROM_NUMBER` | ⚠️ | Twilio sender number in E.164 format |

---

## 📄 License
MIT
