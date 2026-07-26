# LifeLink — Master Feature Roadmap

> **Single source of truth for all planned features.**
> Update the status column as features are completed.
> Reference this at the start of every session.
>
> _An Intelligent Real-Time Blood Donation & Emergency Response Platform_

---

## Legend

| Symbol | Meaning |
|---|---|
| `[x]` | Done |
| `[/]` | In progress |
| `[ ]` | Not started |
| ? | High priority / next to implement |
| ?? | AI / intelligent feature |
| ?? | Security / privacy critical |
| ?? | Scale / social impact |

---

## ? Implemented So Far (Quick Reference)

| Area | What is Done |
|---|---|
| Auth | JWT login/register, role-based access (DONOR, REQUESTER, HOSPITAL_ADMIN) |
| Donor | Service layer, FCM token endpoint, ACCEPT/DECLINE endpoint, active status toggle |
| Geo Matching | PostGIS ST_DWithin, 5km ? 15km ? 30km radius auto-expansion |
| Blood Types | ABO/Rh compatibility matrix, eligibility cooldowns (Whole Blood 90d, Platelets 14d, Plasma 28d) |
| Notifications | Firebase FCM push notifications, priority flag for CRITICAL urgency |
| Scheduled Jobs | Auto radius-expansion every 5 min, auto-expiry check every 60s |
| Blood Chain | Social vouching, one-time 72hr SMS invite links, token lifecycle, Twilio stub |
| Hospital | Full CRUD, PostGIS nearby search, blood-type-filtered nearby search |
| Blood Inventory | Upsert semantics, per-hospital + system-wide availability query |
| User Profiles | GET/PUT /api/users/me, password change, email uniqueness |
| Redis Caching | Hospital geo-queries, compatibility lookups (24h TTL), @CacheEvict on writes |
| Error Handling | Global exception handler: 404, 400 (field-level), 409, 500 |
| DB Migrations | V1 init, V2 seed, V3 blood_chain, V4 hospital indexes |
| Tests | EligibilityUtilTest — partial coverage |

---

## Phase 1 — Core Backend Completion

| # | Feature | Status | Notes |
|---|---|---|---|
| 1 | @EnableScheduling on main class | `[x]` | Present in LifeLinkApplication.java |
| 2 | Donor Service layer | `[x]` | DonorService implemented |
| 3 | FCM token registration — PUT /api/donors/me/fcm-token | `[x]` | Done |
| 4 | Donor ACCEPT / DECLINE — POST /api/requests/{id}/respond | `[x]` | Done |
| 5 | Hospital Service + Controller | `[x]` | Full CRUD + /nearby + /nearby/blood |
| 6 | Blood Inventory Service + Controller | `[x]` | Upsert, per-hospital + system-wide |
| 7 | User profile endpoints | `[x]` | GET/PUT + password change |
| 8 | Redis caching | `[x]` | Geo-queries + compatibility lookups |
| 9 | Pagination — Pageable on all list endpoints | `[ ]` ? | Prevent OOM on large data sets |
| 10 | Request status webhook / polling — notify requester of state changes | `[ ]` ? | EN_ROUTE, DONATED must reach requester |
| 11 | Test suite expansion | `[/]` | EligibilityUtilTest exists; need MatchingEngine, RequestService, auth flow coverage |

---

## Phase 2 — User Management & Authentication (Extended)

> Core JWT auth is done. These items extend registration roles, OTP, and identity verification.

| # | Feature | Status | Notes |
|---|---|---|---|
| 2.1 | Donor registration (email + phone + blood group + location) | `[x]` | JWT register endpoint; blood group + geo on donor entity |
| 2.2 | Hospital registration with admin-verified flag | `[x]` | HOSPITAL_ADMIN role; hospital entity has verified flag |
| 2.3 | Blood Bank registration — separate role + entity | `[ ]` | New BLOOD_BANK_ADMIN role; blood bank entity with geo + operating hours |
| 2.4 | NGO registration — organize camps, recruit donors | `[ ]` | New NGO_ADMIN role; NGO entity |
| 2.5 | Admin Portal — verify hospitals, blood banks, NGOs, moderate reports | `[ ]` | SUPER_ADMIN role; admin-only management endpoints |
| 2.6 | Phone OTP authentication — register/login via OTP | `[ ]` | Twilio Verify or Firebase Phone Auth |
| 2.7 | Google Sign-In / Firebase Authentication | `[ ]` | Firebase Auth token ? local JWT exchange |
| 2.8 | Identity Verification — optional ABHA / government ID | `[ ]` | ?? Hash stored, not raw ID; marks donor as VERIFIED |
| 2.9 | Token refresh — sliding session without re-login | `[ ]` | POST /api/v1/auth/refresh with refresh token |

---

## Phase 3 — Donor Profile & Health Management (Extended)

> Eligibility basics are done. This phase adds detailed health profiling and consent management.

| # | Feature | Status | Notes |
|---|---|---|---|
| 3.1 | Blood group + Rh factor profile | `[x]` | On Donor entity |
| 3.2 | Medical eligibility questionnaire (illness, surgery, medication, pregnancy, tattoos, alcohol) | `[ ]` | New donor_health_questionnaire table; drives eligibility gate |
| 3.3 | Full donation history — date, hospital, blood type, units | `[ ]` | Expand request_responses table; new GET /api/donors/me/history |
| 3.4 | Automatic eligibility checker — next eligible date | `[x]` | EligibilityUtil + cooldown scheduler; exposed via profile endpoint |
| 3.5 | Health status toggle — temporary pause | `[x]` | PUT /api/donors/me/active?active=false |
| 3.6 | Consent management — control profile visibility, location sharing, notifications | `[ ]` | ?? New donor_consent table; checked before each share/notify action |

---

## Phase 4 — Geo-Location & Matching Engine (Extended)

> Radius expansion and basic geo-matching are done. This phase adds ETA intelligence and live tracking.

| # | Feature | Status | Notes |
|---|---|---|---|
| 4.1 | PostGIS ST_DWithin geo matching | `[x]` | In MatchingEngine |
| 4.2 | Auto-expanding radius 5km ? 15km ? 30km | `[x]` | Scheduled job, 5-min intervals |
| 4.3 | Extended radius up to 50km (configurable) | `[ ]` | Add 50km step; configurable via application.yml |
| 4.4 | ETA-based matching — Google Maps Distance Matrix API | `[ ]` ? ?? | Rank donors by travel time, not straight-line distance |
| 4.5 | Live location sharing — accepted donor shares location until arrival | `[ ]` | WebSocket or SSE polling endpoint; auto-expires on DONATED status |
| 4.6 | Geo-fencing alerts — notify users near blood camps / shortage zones | `[ ]` | Background job checks donor location vs. active camp/emergency zone polygons |

---

## Phase 5 — Emergency Blood Request System (Extended)

> Core request flow is done. This phase adds smart ranking, cascading, SOS escalation, and disaster mode.

| # | Feature | Status | Notes |
|---|---|---|---|
| 5.1 | Emergency request creation | `[x]` | POST /api/requests |
| 5.2 | Priority levels (CRITICAL, URGENT, NORMAL) | `[x]` | Urgency enum; FCM priority flag |
| 5.3 | Smart donor ranking — score by distance + eligibility + response history + reputation | `[ ]` ? ?? | Replace first-match with scored ranking in MatchingEngine |
| 5.4 | Cascading notifications — notify top-ranked batch, then next if unanswered | `[ ]` ? | Configurable batch size (default 5); retry scheduler |
| 5.5 | SOS escalation chain: radius expand ? volunteers ? blood banks ? nearby hospitals ? government | `[ ]` ? | Extends existing Blood Chain trigger; configurable escalation steps |
| 5.6 | Blood bank backup suggestion — auto-suggest nearby blood banks if donors unavailable | `[ ]` | Query blood_banks table after 30km exhaustion |
| 5.7 | Cross-hospital request forwarding — unresolved requests sent to neighboring hospitals | `[ ]` | New hospital_request_forwards table |
| 5.8 | Shareable emergency link — public summary of request, zero PII | `[ ]` | GET /api/requests/{id}/share ? public summary |
| 5.9 | Disaster Mode — special mode during floods / earthquakes / mass accidents | `[ ]` ?? | Flag on EmergencyRequest; bypasses radius limits; triggers mass broadcast |

---

## Phase 6 — Smart Notification System (Extended)

> FCM is done. This phase adds fallback channels, filtering, user preferences, and an in-app notification center.

| # | Feature | Status | Notes |
|---|---|---|---|
| 6.1 | Firebase FCM push notifications | `[x]` | NotificationService + FCM Admin SDK |
| 6.2 | SMS fallback — when FCM fails or donor has no token | `[ ]` ? | Twilio; webhook POST /api/webhooks/sms-reply (HMAC-verified) |
| 6.3 | WhatsApp fallback — Twilio WhatsApp API | `[ ]` | Secondary to SMS; template-based messages |
| 6.4 | Notification filtering — only matching blood group + nearby + eligible | `[ ]` | Make explicit filter config per donor; stored in notification_prefs |
| 6.5 | Silent hour settings — custom quiet hours; CRITICAL urgency overrides | `[ ]` | notification_prefs columns on donor; respected in NotificationService |
| 6.6 | Opt-out / STOP — reply STOP to SMS sets sms_opted_out = true | `[ ]` | Handled in SMS webhook reply endpoint |
| 6.7 | In-app notification center — history of all alerts | `[ ]` | New notifications table; GET /api/notifications with pagination |

---

## Phase 7 — Blood Chain & Social Vouching (Extended)

### Feature A — Blood Chain Social Vouching
**Status:** `[x]`

Donors nominate up to 3 trusted contacts. On exhausting 30km with 0 donors, SMS one-time invite links recruit contacts as new donors.
Tables: `donor_vouches`, `blood_chain_invite_tokens`. Token TTL: 72 hours.

---

### Feature K — Blood Chain Trust & Visualization Extensions
**Status:** `[ ]` **Priority:** High ?

Extend Blood Chain with trust scoring, community verification, and referral graph visualization.

| # | Sub-feature | Notes |
|---|---|---|
| K.1 | Trust score — increases when referred by verified donors | New trust_score column on donors; +points per successful referral chain link |
| K.2 | Community verification — multiple verified donors vouch for a new donor | N verified donors can vouch ? auto-upgrades VERIFIED flag |
| K.3 | Blood Chain graph data endpoint | GET /api/donors/me/chain-graph ? node + edge JSON for frontend visualization |
| K.4 | Social impact timeline — anonymous milestones from referral chain | Aggregate stats: chain size, lives helped via chain, expansion over time |

---

### Feature B — Volunteer Driver Network
**Status:** `[ ]` **Priority:** High ?

DRIVER role. Volunteers register location + available hours. When donor accepts but needs transport, system matches a nearby verified driver.

**Plan:**
- New role DRIVER; new table: `drivers(id, user_id, location, is_available, vehicle_type, verified)`
- Geo-match drivers same way as donors (PostGIS ST_DWithin)
- One-time government ID verification (hash stored, not raw ID)
- New FCM notification type: DRIVER_REQUEST
- New endpoint: POST /api/requests/{id}/request-driver

---

### Feature I — Family Blood Pact (Group Pledges)
**Status:** `[ ]` **Priority:** Medium

Opt-in groups (family, apartment, college). Group members notified FIRST before public broadcast.

**Plan:**
- Tables: `blood_pact_groups(id, name, created_by)`, `blood_pact_members(group_id, user_id, joined_at)`
- Membership requires explicit accept; max 50 members (prevent abuse)
- broadcastToDonors() queries eligible group members before public radius
- Group impact dashboard: total units donated by group + badge milestones

---

### Feature L — Community City Networks
**Status:** `[ ]` **Priority:** Medium ??

City-based donor communities. Members receive alerts for local emergencies and camps.

**Plan:**
- Tables: `donor_communities(id, city, name)`, `community_members(community_id, donor_id)`
- Community feed: local donation camps, recent anonymized emergency stats
- GET /api/communities?city=...

---

### Feature M — Corporate CSR Dashboard
**Status:** `[ ]` **Priority:** Medium ??

Companies track employee donations, lives impacted, participation rates for CSR reporting.

**Plan:**
- New role CORPORATE_ADMIN; table: `corporate_groups(id, company_name, admin_id)`
- Donors opt-in to company group
- GET /api/corporate/{id}/dashboard ? aggregate stats, zero PII

---

### Feature N — Campus Ambassador Program
**Status:** `[ ]` **Priority:** Medium ??

College students recruit new donors. Track campus recruitment funnel and leaderboard.

**Plan:**
- New role CAMPUS_AMBASSADOR
- Table: `campus_referrals(ambassador_id, referred_user_id, campus_name, joined_at)`
- Leaderboard: GET /api/leaderboard?type=campus

---

## Phase 8 — Gamification & Motivation

### Feature C — Lifetime Impact Dashboard
**Status:** `[ ]` **Priority:** High ?

Donors see: units donated, lives saved estimate, achievement badges, next eligible date, and anonymous thank-you messages from recipients.

**Plan:**
- Tables: `donor_badges(donor_id, badge_type, earned_at)`, `thank_you_messages(id, request_id, message_text, delivered_at)`
- Badge types: FIRST_DONATION, FIVE_DONATIONS, NIGHT_HERO, RARE_TYPE_DONOR, STREAK_30_DAYS, EMERGENCY_HERO, RARE_BLOOD_CHAMPION, GOLD_DONOR
- Lives saved estimate: 1 unit whole blood ˜ 1 life; 1 platelet ˜ 3 patients
- API: GET /api/donors/me/impact

---

### Feature O — Extended Gamification
**Status:** `[ ]` **Priority:** Medium

| # | Sub-feature | Notes |
|---|---|---|
| O.1 | Experience points (XP) — earn XP for donations, referrals, emergency responses | New donor_xp table; levels: Bronze ? Silver ? Gold ? Platinum |
| O.2 | Leaderboards — by college, company, city, NGO; monthly ranking | Aggregated view refreshed nightly; GET /api/leaderboard |
| O.3 | Life saved counter — show "Estimated 15 lives helped" instead of "5 donations" | Computed field in GET /api/donors/me/impact |
| O.4 | Donation streaks — reward consistent donations with streak badges | Calculated from donation_history; broken if gap > eligibility window |
| O.5 | Birthday donation reminder — annual push notification | Store dob on donor; scheduled job sends reminder 3 days before birthday |
| O.6 | Recipient gratitude messages — anonymous thank-you from recipient or hospital | Hospital submits message post-donation; delivered anonymously to donor |

---

## Phase 9 — Hospital & Blood Bank Platform (Extended)

> Hospital CRUD and blood inventory are done. This phase adds live dashboards, analytics, and cross-institution collaboration.

| # | Feature | Status | Notes |
|---|---|---|---|
| 9.1 | Hospital CRUD + nearby geo search | `[x]` | HospitalService + HospitalController |
| 9.2 | Blood inventory CRUD + availability query | `[x]` | BloodInventoryService + BloodInventoryController |
| 9.3 | Live donor tracking dashboard — Searching ? Accepted ? Travelling ? Arrived ? Donated | `[ ]` ? | Expose RequestStatus stream via SSE or WebSocket |
| 9.4 | Request analytics — avg response time, success rate, completion rate | `[ ]` | Aggregate from request_responses; GET /api/hospitals/{id}/analytics |
| 9.5 | Blood inventory dashboard — Available / Reserved / Critical shortage view | `[ ]` | Computed from blood_inventory; shortage = units < configurable threshold |
| 9.6 | Blood unit reservation — reserve inventory atomically on request acceptance | `[ ]` | reserved_units column; release on cancel/expire |
| 9.7 | Auto shortage alerts for blood banks | `[ ]` | Scheduled job checks inventory < threshold; FCM/SMS to BLOOD_BANK_ADMIN |
| 9.8 | Cross-hospital request forwarding | `[ ]` | New hospital_request_forwards join table; forwarded requests share status stream |

---

## Phase 10 — AI & Intelligent Features

### Feature E — Predictive Blood Shortage Alerts ("Blood Weather Forecast")
**Status:** `[ ]` **Priority:** Medium ??

7-day blood supply forecast by region and blood type. Hospitals get 5-day advance warnings. High-risk type donors get nudges.

**Plan:**
- Scheduled job: weekly, aggregates past 90 days of request data per region
- Simple moving average + day-of-week seasonality (no external ML required)
- Table: `blood_forecasts(id, region, blood_type, forecast_date, predicted_demand, confidence)`
- Alert triggers if predicted demand > rolling average by 30%
- API: GET /api/forecasts?region=...&date=...

---

### Feature P — Extended AI / Intelligence Features
**Status:** `[ ]` **Priority:** Medium ??

| # | Sub-feature | Notes |
|---|---|---|
| P.1 | Availability prediction — predict donor response likelihood from login history, past responses, active status | Scoring model; weight in MatchingEngine donor ranking |
| P.2 | Blood demand prediction — seasonal trends, hospital demand, festivals, accident data | Extends Feature E with richer signals |
| P.3 | Emergency risk prediction — predict high-demand periods; pre-warm donor networks | Notify high-risk-type donors before predicted shortage window |
| P.4 | Fake request detection — AI detects suspicious request patterns | Rule-based first: duplicate IPs, frequency bursts; flag for admin review queue |
| P.5 | Smart donor ranking — multi-factor scored match: distance + eligibility + response history + reputation + ETA | Replaces simple nearest-first in MatchingEngine |
| P.6 | AI Chatbot — answers eligibility questions, myths, nearby camps | Gemini API integration; POST /api/chat |

---

## Phase 11 — Trust, Safety & Compliance

### Feature H — Rare Blood Type Global Registry
**Status:** `[ ]` **Priority:** Critical ?

Opt-in registry for rare blood type donors. On rare blood type emergency, broadcasts nationally, alerts all hospitals, generates a shareable emergency link.

**Plan:**
- New flag is_rare_type BOOLEAN on donors table
- Rare types in config: Bombay (hh), AB-, B-, A-, O- (configurable list)
- National broadcast radius bypasses the 5/15/30km expansion system entirely
- GET /api/requests/{id}/share ? public summary, zero PII
- All standard donor anonymity rules still apply until donor accepts

---

### Feature Q — Trust & Safety Extensions
**Status:** `[ ]` **Priority:** High ?

| # | Sub-feature | Notes |
|---|---|---|
| Q.1 | Hospital verification workflow — admin approves hospitals before they can create requests | verified flag gates POST /api/requests; admin endpoint to approve/reject |
| Q.2 | Donor reputation score — based on completions, response rate, punctuality, verification status | New reputation_score column on donors; updated after each donation event |
| Q.3 | Report & block — users report misuse; admins moderate | New user_reports(reporter_id, reported_id, reason, status) table |
| Q.4 | Emergency audit logs — immutable record of every action | New audit_log(id, actor_id, action, entity_type, entity_id, timestamp, payload_json) |
| Q.5 | Rate limiting — prevent spam and abuse | Bucket4j or Spring @RateLimiter; per-IP + per-user limits on request creation |
| Q.6 | Anonymous chat channel — hospitals and donors communicate without revealing phone numbers | Anonymous channel ID; phone number revealed only after ACCEPT |
| Q.7 | Consent management — profile visibility, location sharing, contact disclosure, notification prefs | donor_consent table; checked before every share/notify action |

---

## Phase 12 — Analytics & Public Reporting

### Feature J — Anonymized Public Health Reports
**Status:** `[ ]` **Priority:** Medium

Auto-generated weekly anonymized aggregate reports: regional demand heatmaps, average time-to-fulfillment, shortage patterns, donor density gaps.

**Plan:**
- Scheduled job: every Sunday at midnight
- Table: `public_health_reports(id, report_date, region, data_json)`
- Zero PII: all data is aggregated counts and averages
- Public endpoint (no auth): GET /api/public/health-reports
- Output: JSON + optional CSV download

---

### Feature R — Extended Analytics Dashboard
**Status:** `[ ]` **Priority:** Medium

| # | Sub-feature | Notes |
|---|---|---|
| R.1 | Blood demand heatmaps — city/district level | Aggregate requests by geo-cell; GET /api/analytics/heatmap |
| R.2 | Blood group statistics — availability by type nationally | Computed from blood_inventory; GET /api/analytics/blood-stats |
| R.3 | Response analytics — avg donor response time by region and blood type | From request_responses; GET /api/analytics/response-times |
| R.4 | Donation trends — weekly, monthly, yearly time series | Time-series query on donation_history |
| R.5 | Platform KPIs — active donors, successful matches, avg search time, success %, unfulfilled requests | GET /api/analytics/kpis (SUPER_ADMIN only) |

---

## Phase 13 — Post-Donation Health & Recovery

### Feature F — Post-Donation Health Companion
**Status:** `[ ]` **Priority:** Medium

After DONATED status confirmed, trigger a 7-day push notification recovery protocol: hydration tips, iron-rich food reminders, wellness check-ins.

**Plan:**
- Table: `donor_recovery_schedules(donor_id, donation_date, day_index, sent_at)`
- Scheduled job: daily check for pending recovery messages
- Day 3 and Day 7 include a 1–5 wellness self-rating (one tap, in-app)
- Wellness data internal only, never shared externally

---

## Phase 14 — Accessibility, Localization & Digital Documents

| # | Feature | Status | Notes |
|---|---|---|---|
| 14.1 | Multi-language support — EN, Hindi, Kannada, Tamil, Telugu, Malayalam, Marathi | `[ ]` | i18n message bundles; Accept-Language header respected by all endpoints |
| 14.2 | Voice assistance — accessibility for elderly and visually impaired | `[ ]` | Text-to-speech hints in API responses; dedicated voice-friendly endpoints |
| 14.3 | Offline emergency mode — limited functionality without internet | `[ ]` | Cached donor cards + SMS fallback (Feature 6.2 prerequisite) |
| 14.4 | Digital donor card — QR code, blood group, donation history, eligibility, emergency contact | `[ ]` | GET /api/donors/me/card ? signed JWT-embedded QR image |
| 14.5 | Donation certificate — auto-generated after every DONATED status confirmation | `[ ]` | PDF via Apache PDFBox or JasperReports; emailed to donor |
| 14.6 | Medical records storage — optional, consent-based | `[ ]` | ?? Encrypted blob storage; access gated by donor_consent flag |

---

## Phase 15 — Community, Camps & Social Impact

### Feature G — Community Camp Organizer Portal
**Status:** `[ ]` **Priority:** High ?

CAMP_ORGANIZER role to schedule donation camps, publish time-slot sign-ups via shareable links, and provide a live dashboard on camp day.

**Plan:**
- New role: CAMP_ORGANIZER
- Tables: `donation_camps(id, organizer_id, location, date, target_units, status)`, `camp_registrations(id, camp_id, donor_id, slot_time, attended)`
- Public camp page accessible without login (view-only)
- Shareable registration link with short code
- Geo-fencing: notify donors within radius when camp date approaches

---

### Feature S — Disaster Mode
**Status:** `[ ]` **Priority:** High ??

Special emergency mode during mass-casualty events (floods, earthquakes, mass accidents).

**Plan:**
- disaster_mode boolean flag on EmergencyRequest or system-wide config toggle
- Bypasses all radius limits; broadcasts nationally for rare types or to all eligible donors within 100km
- Admin-only toggle: POST /api/admin/disaster-mode?active=true
- Auto-expires after 24 hours or manual admin deactivation
- Distinct FCM notification type and priority: DISASTER

---

## Phase 16 — Security & Infrastructure Hardening

| # | Feature | Status | Notes |
|---|---|---|---|
| 16.1 | JWT auth + RBAC | `[x]` | JwtFilter, SecurityConfig, Role enum |
| 16.2 | Redis caching | `[x]` | RedisConfig, @Cacheable on geo + compatibility queries |
| 16.3 | Firebase FCM | `[x]` | FirebaseConfig, NotificationService |
| 16.4 | Rate limiting — prevent spam and abuse | `[ ]` ? ?? | Bucket4j or Spring @RateLimiter; per-IP + per-user limits |
| 16.5 | End-to-end encryption for anonymous chat | `[ ]` ?? | AES-GCM for chat messages; keys never leave client |
| 16.6 | Background workers — async notification delivery, retries, analytics | `[ ]` | Spring @Async + ThreadPoolTaskExecutor; Dead Letter Queue for failed notifications |
| 16.7 | Health monitoring + metrics | `[ ]` | Spring Boot Actuator + Micrometer ? Prometheus / Grafana |
| 16.8 | Crash reporting | `[ ]` | Sentry SDK for structured error tracking and alerting |
| 16.9 | Automated DB backups | `[ ]` | pg_dump cron job or managed backup policy in Docker Compose / cloud |
| 16.10 | API versioning strategy | `[ ]` | Confirm /api/v1/ convention across all new endpoints; document deprecation policy |
| 16.11 | OpenAPI / Swagger completeness | `[ ]` | Annotate all endpoints with @Operation, @ApiResponse, @SecurityRequirement |

---

## Phase 17 — Frontend

> To be designed after backend API surface is stable.

| # | Component | Status | Notes |
|---|---|---|---|
| F.1 | Donor dashboard with Impact visualization | `[ ]` | React or Flutter Web; mobile-first |
| F.2 | Emergency request form (Requester / Hospital) | `[ ]` | Real-time status feedback |
| F.3 | Live request status tracker | `[ ]` | WebSocket or SSE feed |
| F.4 | Blood Chain referral and visualization | `[ ]` | Interactive graph component |
| F.5 | Hospital admin panel | `[ ]` | Request management, inventory, analytics |
| F.6 | Blood bank dashboard | `[ ]` | Inventory management + shortage alerts |
| F.7 | NGO / Camp organizer portal | `[ ]` | Camp scheduling + QR sign-in on camp day |
| F.8 | Admin moderation portal | `[ ]` | Verification queue, reports, audit logs |
| F.9 | Public health reports page | `[ ]` | Heatmaps, KPI stats (no auth required) |
| F.10 | Multi-language UI | `[ ]` | i18n integration (see Phase 14) |

---

## ? Unique Selling Features (Differentiators)

| # | Feature | Phase | Status |
|---|---|---|---|
| 1 | Blood Chain — trusted donor referral + verification network | 7 | `[x]` |
| 2 | AI-Based Smart Donor Ranking — most likely to respond, not just nearest | 10 | `[ ]` |
| 3 | Dynamic Search Radius — expands automatically until match found | 4 | `[x]` |
| 4 | ETA-Based Matching — arrival time over straight-line distance | 4 | `[ ]` |
| 5 | Cascading Notifications — ranked batches prevent alert fatigue | 5 | `[ ]` |
| 6 | Privacy-First Anonymous Communication — identity revealed only on consent | 11 | `[x]` (design) |
| 7 | Cross-Hospital Collaboration — forward unresolved requests securely | 9 | `[ ]` |
| 8 | Blood Demand Prediction — forecasts shortages before they occur | 10 | `[ ]` |
| 9 | Real-Time Blood Demand Heatmaps — actionable insights for hospitals and authorities | 12 | `[ ]` |
| 10 | Disaster Mode — rapid coordinated response during mass emergencies | 15 | `[ ]` |
| 11 | Community Trust and Reputation System — verified participation builds confidence | 11 | `[ ]` |
| 12 | Social Impact Dashboard — lives helped, not just donation counts | 8 | `[ ]` |

---

## Suggested Implementation Priority Order

```
Phase 1 (remaining: #9 pagination, #10 webhook)
    ? Phase 5 (Emergency System: cascading, SOS, disaster mode)
    ? Phase 6 (Notifications: SMS fallback, filtering, silent hours)
    ? Phase 4 (Matching: ETA, live location)
    ? Phase 9 (Hospital/Blood Bank platform extensions)
    ? Phase 7 (Blood Chain extensions + Driver Network)
    ? Phase 8 (Gamification + Impact Dashboard)
    ? Phase 11 (Trust, Safety, Rare Type Registry)
    ? Phase 10 (AI features)
    ? Phase 12 (Analytics)
    ? Phase 13 (Post-Donation Recovery)
    ? Phase 2 (Extended Auth: OTP, Google SSO, identity)
    ? Phase 3 (Health profiling + consent)
    ? Phase 15 (Community, Camps, Disaster Mode)
    ? Phase 14 (Accessibility, Documents, i18n)
    ? Phase 16 (Infrastructure hardening)
    ? Phase 17 (Frontend)
```

---

## Session Notes

> Add a note after each session so the next session picks up where we left off.

### Session 1 — 2026-07-24
- Full project analysis completed
- @EnableScheduling already present — checked off
- EligibilityUtilTest already exists — test suite started
- Created this ROADMAP.md
- **Completed this session:** Phase 1 features #2, #3, #4 (Donor Service, FCM token endpoint, Donor respond endpoint)
- **Next session start:** Phase 1 Feature #5 — Hospital Service + Controller

### Session 2 — 2026-07-24
- Implemented Feature A: Blood Chain (Social Vouching Network) completely
- New package: com.lifelink.bloodchain
  - DonorVouch entity + DonorVouchRepository
  - BloodChainInviteToken entity + BloodChainInviteTokenRepository
  - BloodChainService — vouch CRUD, Blood Chain activation, token lifecycle
  - BloodChainController — vouch management + public invite validation endpoints
- New DTOs: AddVouchDto, VouchDto, InviteDetailsDto
- New SmsService (stub, Twilio-ready with commented real implementation)
- RequestService.broadcastToDonors() — now activates Blood Chain when 30km radius finds 0 donors
- AuthController — new POST /api/v1/auth/register/invited?token= endpoint
- SecurityConfig — blood-chain invite URLs made public
- application.yml — added Twilio + app.base-url config
- DB migration V3__blood_chain.sql — donor_vouches + blood_chain_invite_tokens tables
- JDK 21 installed via winget (was JDK 17). Set JAVA_HOME for compile.
- **Next session start:** Feature #5 — Hospital Service + Controller

### Session 3 — 2026-07-25
- Implemented Feature #5: Hospital Service + Controller
- Implemented Feature #6: Blood Inventory Service + Controller
- New files: HospitalRepository, BloodInventoryRepository, HospitalService, BloodInventoryService, HospitalController, BloodInventoryController
- New DTOs: HospitalDto, CreateHospitalDto, NearbyHospitalDto, InventoryDto, UpdateInventoryDto
- Updated Hospital entity: added address, contactPhone, createdAt, cascade @OneToMany to inventory
- DB migration V4__hospital_inventory_indexes.sql — new columns + GIST/lookup indexes
- JAVA_HOME must be set to C:\Users\anuvu\.jdks\jdk-21.0.11+10 before running mvnw
- BUILD SUCCESS confirmed
- Implemented Feature #7: User profile endpoints
- New files: UserService, UserController, DTOs: UserProfileDto, UpdateProfileDto, ChangePasswordDto
- Upgraded GlobalExceptionHandler: 404 (EntityNotFoundException), 400 (field-level), 409 (DataIntegrityViolationException)
- Implemented Feature #8: Redis caching
- New files: RedisConfig (JSON serializer, per-cache TTLs), CacheNames (constants)
- @Cacheable on: HospitalService.getAll(), findNearby(), findNearbyWithBlood(), CompatibilityMatrix.getCompatibleDonors() (24h TTL)
- @CacheEvict on all hospital + inventory write methods
- CompatibilityMatrix converted from static utility to Spring @Component so AOP proxy works
- MatchingEngine updated to inject CompatibilityMatrix bean
- spring.cache.type=redis added to application.yml
- **Roadmap expanded** — merged comprehensive 18-category feature set (100+ features across 17 phases) into master ROADMAP.md
- **Next session start:** Phase 1 Feature #9 — Pagination (Pageable on all list endpoints), then #10 — Request status webhook
