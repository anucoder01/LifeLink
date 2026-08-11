# LifeLink - Comprehensive Project History & Changelog

## [1.0.0] - Production Ready (Current)

### Added
- **Modern React Frontend**: Fully responsive, glassmorphism-themed frontend using Vite and React.
- **Authentication**: JWT-based Secure Login and Registration forms with `react-phone-number-input` for international dialing.
- **Real-time Geolocation**: HTML5 Geolocation integration replacing dummy data, pinpointing exact GPS coordinates for accurate spatial matching.
- **Donor Dashboard**: Advanced dashboard to view location, manage availability status, and track Blood Chain contacts.
- **Emergency SOS Feed**: Real-time localized feed of critical blood requests with Acceptance/Decline workflows.
- **My Requests Hub**: A dedicated management portal for SOS requesters to track live donor responses, request volunteer drivers, and mark requests as Fulfilled or Canceled.
- **Hospital Admin Portal**: Dashboard for hospital administrators to manage Webhook subscriptions (e.g. subscribing to `DONOR_MATCHED` events) and view blood bank inventory metrics.
- **Blood Chain Network (Social Vouching)**: The core fallback system. Includes APIs and UI to nominate up to 3 trusted contacts who receive SMS invites via Twilio when no registered donors are found.
- **Geo-targeted Matching Engine**: Robust backend PostGIS (`ST_DWithin`) spatial queries enforcing radius expansion (5km → 15km → 30km).
- **Scheduled Jobs**: Automated tasks for radius-expansion (every 5 min) and request expiry checks (every 60s).
- **Push Notifications (FCM)**: Integration with Firebase Admin SDK for instant alerts.
- **Webhook Infrastructure**: Complete webhook dispatch system for real-time B2B integration.

### Fixed
- **Backend Test Suite**: Resolved all Mockito `PotentialStubbingProblem`s and `NullPointerExceptions` related to missing repository mocks and uninitialized config values (e.g., `maxRadiusKm` using Java Reflection).
- **Pagination & Error Handling**: Refined global exception handlers and `PaginationUtil` standardization across all paginated endpoints.
- **Frontend Prop Drilling**: Fixed React component styling crashes where the `style` prop wasn't properly passed down in `PrimaryButton.jsx`.

### Documentation
- **Presentation Guide**: Created `project_presentation_guide.md` covering the WhatsApp problem statement, architecture, Twilio/PostGIS tech stack, and future roadmaps for faculty defense.
- **Readme**: Overhauled `README.md` to document environment variables, architecture diagrams, API specs, and the newly integrated UI features.
