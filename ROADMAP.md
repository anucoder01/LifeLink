# LifeLink — Master Feature Roadmap

> This file is the single source of truth for all planned features.
> Update the status column as features are completed.
> Reference this at the start of every session.

---

## Legend
- `[ ]` Not started
- `[/]` In progress
- `[x]` Done

---

## Phase 1 — Core Backend Completion (Fix the Skeleton)

| # | Feature | Status | Notes |
|---|---|---|---|
| 1 | `@EnableScheduling` on main class | `[x]` | Already present in `LifeLinkApplication.java` |
| 2 | **Donor Service layer** — business logic between controller and repo | `[ ]` | `DonorController` calls repo directly; needs a `DonorService` |
| 3 | **FCM token registration endpoint** — `PUT /api/donors/fcm-token` | `[ ]` | Donors must be able to register/update their FCM token post-login |
| 4 | **Donor ACCEPT / DECLINE endpoint** — `POST /api/requests/{id}/respond` | `[ ]` | Donors respond to a notification; updates `RequestResponseStatus` |
| 5 | **Hospital Service + Controller** — full CRUD and geo-query | `[ ]` | Entities exist, no service/controller |
| 6 | **Blood Inventory Service + Controller** — stock CRUD and availability query | `[ ]` | Entities exist, no service/controller |
| 7 | **User profile endpoints** — `GET /api/users/me`, `PUT /api/users/me` | `[ ]` | User entity exists, no management API |
| 8 | **Redis caching** — cache donor geo-queries and blood type lookups | `[ ]` | Redis is configured, `@Cacheable` is unused |
| 9 | **Pagination** — all list endpoints must support `Pageable` | `[ ]` | Prevent OOM on large data sets |
| 10 | **Request status webhook / polling** — notify requester of donor state changes | `[ ]` | EN_ROUTE, DONATED status changes must reach the requester |
| 11 | **Test suite expansion** — `MatchingEngine`, `RequestService`, auth flow | `[/]` | `EligibilityUtilTest` exists; need more coverage |

---

## Phase 2 — Unique & Creative Features (High Social Impact)

### Feature A — Blood Chain (Social Vouching Network)
**Status:** `[x]`
**Priority:** High

Each donor nominates 2-3 trusted contacts as "backup donors". When the system exhausts its radius (30km, 0 donors found), it sends a one-click opt-in SMS to these contacts to recruit them at the moment of real need.

**Implementation plan:**
- New table: `donor_vouches(donor_id, contact_phone, contact_name)` — no PII stored until contact opts in
- Triggered after max radius (30km) is reached with 0 donors found
- Twilio / MSG91 SMS integration
- Contact receives a short-lived one-time link to register as a donor

---

### Feature B — Volunteer Driver Network
**Status:** `[ ]`
**Priority:** High

A `DRIVER` user role. Volunteers register location + available hours. When a donor accepts a request but has no transport, the system simultaneously matches a nearby driver and offers them to the donor.

**Implementation plan:**
- New role `DRIVER` in `Role` enum
- New table: `drivers(id, user_id, location, is_available, vehicle_type, verified)`
- Geo-match drivers same way donors are matched (PostGIS ST_DWithin)
- One-time government ID verification (hash stored, not raw ID)
- New FCM notification type: `DRIVER_REQUEST`

---

### Feature C — Lifetime Impact Dashboard
**Status:** `[ ]`
**Priority:** High

Donors see: units donated, lives saved estimate, anonymized impact area map, achievement badges, countdown to next eligible date, and anonymous thank-you messages from recipients.

**Implementation plan:**
- New table: `donor_badges(donor_id, badge_type, earned_at)`
- New table: `thank_you_messages(id, request_id, message_text, delivered_at)` — no PII
- Badge types: FIRST_DONATION, FIVE_DONATIONS, NIGHT_HERO, RARE_TYPE_DONOR, STREAK_30_DAYS
- Lives saved estimate: 1 unit whole blood ≈ 1 life; 1 platelet ≈ 3 patients
- Analytics endpoint: `GET /api/donors/me/impact`

---

### Feature D — SMS / WhatsApp Fallback Notifications
**Status:** `[ ]`
**Priority:** Very High (highest ROI)

When FCM fails or donor has no FCM token, fall back to SMS (Twilio). Donor replies YES/NO via SMS; a webhook parses replies and updates RequestResponseStatus.

**Implementation plan:**
- Integrate Twilio or MSG91
- New env vars: `TWILIO_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`
- Webhook endpoint: `POST /api/webhooks/sms-reply` (public, HMAC-verified)
- Opt-out: reply STOP → sets `donor.sms_opted_out = true`

---

### Feature E — Predictive Shortage Alerts ("Blood Weather Forecast")
**Status:** `[ ]`
**Priority:** Medium

Analyze historical request + fulfillment patterns to generate a 7-day blood supply forecast by region and blood type. Hospitals get 5-day advance warnings. Donors of high-risk types get nudges.

**Implementation plan:**
- Scheduled job: runs weekly, aggregates past 90 days of request data per region
- Simple moving average + day-of-week seasonality (no external ML needed)
- New table: `blood_forecasts(id, region, blood_type, forecast_date, predicted_demand, confidence)`
- Alert triggers if predicted demand > rolling average by 30%
- API: `GET /api/forecasts?region=...&date=...`

---

### Feature F — Post-Donation Health Companion
**Status:** `[ ]`
**Priority:** Medium

After DONATED status confirmed, trigger a 7-day push notification recovery protocol: hydration tips, iron-rich food reminders, wellness check-ins.

**Implementation plan:**
- New table: `donor_recovery_schedules(donor_id, donation_date, day_index, sent_at)`
- Scheduled job checks daily for pending recovery messages
- Day 3 and Day 7 include a 1-5 wellness self-rating (one tap)
- Wellness data used only internally, never shared

---

### Feature G — Community Camp Organizer Portal
**Status:** `[ ]`
**Priority:** High

A `CAMP_ORGANIZER` role to schedule donation camps, publish time-slot sign-ups via shareable links, and get a live dashboard on camp day.

**Implementation plan:**
- New role: `CAMP_ORGANIZER`
- New tables: `donation_camps(id, organizer_id, location, date, target_units, status)`
- New table: `camp_registrations(id, camp_id, donor_id, slot_time, attended)`
- Public camp page accessible without login (view-only)
- Shareable registration link with short code

---

### Feature H — Rare Blood Type Global Registry
**Status:** `[ ]`
**Priority:** Critical (life-saving)

Opt-in registry for rare blood type donors. On a rare blood type emergency, broadcasts nationally, alerts all hospitals, and generates a shareable emergency link.

**Implementation plan:**
- New flag on `donors` table: `is_rare_type BOOLEAN`
- Rare types defined in config: Bombay, AB-, B-, A-, O- (configurable)
- National broadcast radius bypasses the 5/15/30 km expansion system
- Shareable emergency link: `GET /api/requests/{id}/share` → public summary, no PII
- All standard donor anonymity rules still apply until donor accepts

---

### Feature I — Family Blood Pact (Group Pledges)
**Status:** `[ ]`
**Priority:** Medium

Opt-in groups (family, apartment, college). When any member creates an emergency request, the group is notified FIRST before public broadcast.

**Implementation plan:**
- New tables: `blood_pact_groups(id, name, created_by)`, `blood_pact_members(group_id, user_id, joined_at)`
- Membership requires explicit accept (no auto-adding)
- During broadcastToDonors(), first query eligible donors who are group members of requester
- Group impact dashboard: total units donated by group, group badge milestones
- Max group size: 50 (prevent abuse)

---

### Feature J — Anonymized Public Health Reports
**Status:** `[ ]`
**Priority:** Medium

Auto-generated weekly anonymized aggregate reports: regional demand heatmaps, average time-to-fulfillment, shortage patterns, donor density gaps. Published via public API for NGOs and health ministries.

**Implementation plan:**
- Scheduled job: runs every Sunday at midnight
- New table: `public_health_reports(id, report_date, region, data_json)`
- Zero PII: all data is aggregated counts and averages
- Public endpoint (no auth): `GET /api/public/health-reports`
- Output: JSON + optional CSV download

---

## Phase 3 — Frontend (To be planned)
- Mobile-first web app (React or Flutter web)
- Donor dashboard with Impact visualization
- Requester emergency form
- Live request status tracker
- Camp organizer portal UI
- Hospital admin panel

---

## Session Notes

> Add a note after each session so the next session picks up where we left off.

### Session 1 — 2026-07-24
- Full project analysis completed
- `@EnableScheduling` already present — checked off
- `EligibilityUtilTest` already exists — test suite started
- Created this ROADMAP.md
- **Completed this session:** Phase 1 features #2, #3, #4 (Donor Service, FCM token endpoint, Donor respond endpoint)
- **Next session start:** Phase 1 Feature #5 — Hospital Service + Controller

### Session 2 — 2026-07-24
- Implemented Feature A: Blood Chain (Social Vouching Network) completely
- New package: `com.lifelink.bloodchain`
  - `DonorVouch` entity + `DonorVouchRepository`
  - `BloodChainInviteToken` entity + `BloodChainInviteTokenRepository`
  - `BloodChainService` — vouch CRUD, Blood Chain activation, token lifecycle
  - `BloodChainController` — vouch management + public invite validation endpoints
- New DTOs: `AddVouchDto`, `VouchDto`, `InviteDetailsDto`
- New `SmsService` (stub, Twilio-ready with commented real implementation)
- `RequestService.broadcastToDonors()` — now activates Blood Chain when 30km radius finds 0 donors
- `AuthController` — new `POST /api/v1/auth/register/invited?token=` endpoint
- `SecurityConfig` — blood-chain invite URLs made public
- `application.yml` — added Twilio + app.base-url config
- DB migration `V3__blood_chain.sql` — `donor_vouches` + `blood_chain_invite_tokens` tables
- JDK 21 installed via winget (was JDK 17). Set JAVA_HOME for compile.
- **Next session start:** Feature #5 — Hospital Service + Controller
