# LifeLink


LifeLink is a blood donation and emergency coordination prototype. It combines a Spring Boot API, PostgreSQL/PostGIS, Redis, and a React web client. The project is intended for local evaluation and a supervised pilot; it is not a substitute for hospital transfusion workflows.

## Implemented

- Phone and password authentication with JWTs, donor/requester self-registration, role-based authorization, and administrator-created hospital accounts.
- Geo-targeted donor matching with blood compatibility and donation eligibility checks, radius expansion, FCM notifications, and SMS/WhatsApp fallback through Twilio.
- Emergency requests with donor response tracking, request history, SSE updates, driver requests, hospital stock suggestions, and an administrator verification queue. New requests are not matched or broadcast until a `SUPER_ADMIN` verifies them.
- Blood Chain invitations to a donor's trusted contacts when matching has no eligible donors at the configured maximum radius.
- Hospital stock totals and per-lot tracking for collection date, expiry, temperature check history, quarantine, and inter-hospital handoff/receipt. The aggregate stock total is updated from the tracked lots when they are created, quarantined, transferred, or expired.
- Stock freshness endpoint reports the oldest hospital stock confirmation and whether it is older than `INVENTORY_STALE_HOURS` (24 hours by default).
- Donor consent settings, donation history, FCM token management, availability controls, recovery check-ins, and impact badges/leaderboard/certificates.
- React flows for sign-in, donor registration, request submission and status, live donor locations, gamification/recovery, and hospital lot management.

Some other pages (such as the public requests map, camps, NGO, and driver portals) remain prototypes and still contain sample content or simulated actions. Check the page and API before using any function in a live workflow.

## Stack

- Java 17+, Spring Boot 3.3, Spring Security, JWT, Flyway
- PostgreSQL 15 with PostGIS; Redis 7
- React, Vite, Leaflet
- Firebase Cloud Messaging; optional Twilio SMS and WhatsApp

## Run locally

Prerequisites: Java 17 or newer, Node.js 20 or newer, and Docker Compose.

1. Copy `.env.example` to `.env`. Replace `JWT_SECRET` with a random secret of at least 32 bytes. Add Firebase and Twilio credentials only when those services are configured.
2. Start the database and cache:

   ```sh
   docker compose up -d postgres redis backup
   ```

3. Export the required environment variables into the shell running Spring Boot, then start the API:

   ```sh
   ./mvnw spring-boot:run
   ```

   On Windows use `mvnw.cmd spring-boot:run`. Flyway applies the database migrations on startup.

4. Start the web client in another shell:

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

   Copy `frontend/.env.example` to `frontend/.env.local` to configure browser settings. `VITE_API_BASE_URL` can point the client at a different API host; it defaults to `http://localhost:8080`. Google Maps is optional; set `VITE_GOOGLE_MAPS_API_KEY` to use it. If the key is unset or rejected, maps continue to use OpenStreetMap.

API documentation is available at `http://localhost:8080/swagger-ui.html`.

## Account setup and access

- Public registration only accepts `DONOR` and `REQUESTER` roles. Hospital and super-administrator roles must not be supplied through public registration.
- A super-administrator account must be provisioned out of band by the deployment operator. The project does not create a default administrator password.
- A `SUPER_ADMIN` verifies a hospital, then creates its administrator with `POST /api/v1/admin/hospital-admins`. Hospital administrators can access only the hospital assigned to their account.
- A `SUPER_ADMIN` reviews new requests through `GET /api/v1/admin/requests/pending-verification` and approves one with `PUT /api/v1/admin/requests/{id}/verify`. Approval starts inventory lookup and donor matching.

## Useful API routes

| Route | Purpose |
|---|---|
| `POST /api/v1/auth/register` and `/login` | Register as donor/requester or sign in |
| `POST /api/v1/requests` | Submit an emergency request for verification |
| `GET /api/v1/requests` | List the signed-in requester's requests |
| `PUT /api/v1/admin/requests/{id}/verify` | Approve a request and begin matching (super-admin) |
| `GET /api/v1/hospitals/inventory/availability/status` | Read stock totals and freshness by blood type/component |
| `/api/v1/hospitals/{hospitalId}/lots` | Create and list blood-unit lots (assigned hospital admin) |
| `/api/v1/hospitals/{hospitalId}/lots/{lotId}/temperature` | Record a cold-chain check; an out-of-range report quarantines the lot |
| `/api/v1/hospitals/{hospitalId}/lots/{lotId}/handoffs` | Record units entering transit |
| `/api/v1/hospitals/{hospitalId}/lots/handoffs/{handoffId}/receive` | Confirm receipt at the destination hospital |
| `GET /api/v1/donors/me/gamification` and `/recovery` | Read the signed-in donor's own impact and check-in data |
| `POST /api/v1/webhooks/twilio/inbound-sms` | Process signed SMS replies such as `YES <full-request-id>`, `NO`, `STATUS`, `STOP`, and `START` |

## Configuration

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | PostgreSQL/PostGIS connection |
| `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` | Redis connection |
| `JWT_SECRET` | Required signing key (at least 32 bytes); no built-in production default |
| `FIREBASE_CONFIG_PATH` | Firebase service-account JSON path; push notifications are unavailable until configured |
| `APP_BASE_URL` | Public application URL used in Blood Chain invitations |
| `TWILIO_ENABLED` | Enables provider delivery; defaults to `false` |
| `TWILIO_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER` | Twilio SMS credentials and sender |
| `TWILIO_WHATSAPP_FROM_NUMBER` | Approved Twilio WhatsApp sender, if used |
| `TWILIO_PUBLIC_WEBHOOK_URL` | Exact public URL configured as the Twilio inbound callback; required for signature validation |
| `INVENTORY_STALE_HOURS` | Age after which stock is marked stale (default 24) |
| `BACKUP_INTERVAL_SECONDS`, `BACKUP_RETENTION_DAYS` | Scheduled PostgreSQL dump interval and local retention period for the optional Compose backup service |

The Google Maps JavaScript API key is a frontend setting in `frontend/.env.local`, not the backend `.env`. Create a key in Google Cloud, enable the Maps JavaScript API and billing, then restrict the key to that API and your local/deployed web referrers. Browser keys are visible to users, so apply those restrictions and usage limits. See [Google's API key setup guide](https://developers.google.com/maps/documentation/javascript/get-api-key).

## Operational limits to address before a real deployment

- Request review is currently performed by a `SUPER_ADMIN`; a hospital-specific verification workflow and evidence handling need to be agreed with pilot partners.
- Lot temperatures are operator-entered and the in-range flag is supplied by the operator. Connect validated temperature devices and configure clinically approved ranges before treating these records as cold-chain assurance.
- The donor recovery panel is a self-reported log, not medical advice. Recovery content should be reviewed by qualified clinicians for the pilot region.
- The Compose backup service writes compressed database dumps to `./backups`; store copies off-host and test restores before relying on them. Configure verified Firebase/Twilio accounts, public HTTPS callback URLs, monitoring, and data-retention procedures. Do not use sample data or local credentials for real patient care.
- The database is required for the full automated test suite. Start PostgreSQL/PostGIS and Redis before running integration tests.

## Verification

```sh
./mvnw test
cd frontend && npm run build
```

Windows:

```powershell
./mvnw.cmd test
cd frontend
npm.cmd run build
```

## License

MIT
