# Hospital Management System (Gastroenterology)

[![Backend Tests](https://github.com/jayeshfw230025/Hospital-Mangment-system/actions/workflows/backend-tests.yml/badge.svg)](https://github.com/jayeshfw230025/Hospital-Mangment-system/actions/workflows/backend-tests.yml)
[![Frontend CI](https://github.com/jayeshfw230025/Hospital-Mangment-system/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/jayeshfw230025/Hospital-Mangment-system/actions/workflows/frontend-ci.yml)

Full-stack HMS for a gastroenterology practice: Spring Boot 3.3 backend covering
patient registration, OPD/IPD clinical workflows, investigations, e-prescription,
clinical decision support, admissions/discharge, analytics, auth/RBAC, and
FHIR/ABDM integration, plus a React 18 + TypeScript frontend covering the full
patient journey from registration through OPD/IPD encounters.

## Prerequisites

- Java 17+ (JDK 21 recommended)
- Node.js 18+ and npm
- MySQL 8.0 (optional — see [Running the backend](#running-the-backend) for a
  no-MySQL local option)
- Docker (optional, only needed if you want MySQL via `docker-compose`)

## Running the backend

The backend ships with two Spring profiles:

### Option A — local profile (no MySQL/Docker required)

Uses a file-based H2 database (`./data/hms-local`), so you can run the whole
stack with nothing installed but a JDK.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

### Option B — default profile (MySQL)

1. Start MySQL:
   ```bash
   docker-compose up -d
   ```
   This starts MySQL 8.0 on port `3306` with database `hms_db`, user
   `hms_user` / `hms_password` (see `docker-compose.yml`).
2. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```

Either way, the backend starts on **http://localhost:8080**, applies Flyway
migrations automatically, and — on first startup only — seeds one demo user
per role (see [Demo accounts](#demo-accounts) below).

Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Running tests

```bash
./mvnw test
```

Tests run against an in-memory H2 database (`test` profile) and do not
require MySQL or Docker.

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on **http://localhost:5173** and proxies `/api` requests
to `http://localhost:8080` (see `frontend/vite.config.ts`), so the backend
must be running first.

## Demo accounts

On first startup, the backend seeds one user per role with the password
below. Login is a two-step flow: submit username/password, then an OTP that
is logged to the backend console (dev stub — no real SMS/email gateway is
wired up).

| Username         | Role       |
|------------------|------------|
| `doctor.demo`     | DOCTOR     |
| `nurse.demo`      | NURSE      |
| `admin.demo`      | ADMIN      |
| `pharmacist.demo` | PHARMACIST |
| `dietitian.demo`  | DIETITIAN  |

Password for all demo accounts: `Passw0rd!23`

These are for local development/demo only — replace or remove before any
real deployment.

## Project structure

```
hospital-management-system/
├── src/main/java/com/hms/      # Backend modules (patient, opd, ipd, clinical,
│                                # investigation, prescription, cds, discharge,
│                                # analytics, auth, integration, ...)
├── src/main/resources/         # application.yml, application-local.yml, Flyway migrations
├── src/test/                   # Integration tests (H2, `test` profile)
├── docker-compose.yml          # Local MySQL for the default profile
└── frontend/                   # React 18 + TypeScript + Vite + Tailwind SPA
    └── src/
        ├── api/                 # One API client module per backend domain
        ├── pages/                # Route-level pages, grouped by domain
        └── components/          # Shared layout/UI components
```

## Configuration

Backend configuration is environment-variable driven (see
`src/main/resources/application.yml`); notable overrides:

| Variable            | Default                     | Purpose                     |
|----------------------|------------------------------|------------------------------|
| `DB_HOST` / `DB_PORT` | `localhost` / `3306`         | MySQL connection (default profile) |
| `DB_NAME`             | `hms_db`                     | MySQL database name          |
| `DB_USER` / `DB_PASSWORD` | `hms_user` / `hms_password` | MySQL credentials             |
| `SERVER_PORT`         | `8080`                        | Backend HTTP port             |
| `HMS_JWT_SECRET`      | dev default (insecure)        | JWT signing key — **override in any shared/deployed environment** |
| `HMS_CRYPTO_KEY`      | dev default (insecure)        | Field-level encryption key — **override in any shared/deployed environment** |

## Known gaps

- No Notifications module UI — there is no dedicated notifications backend
  capability to wire against (WhatsApp dispatch is only stubbed inside the
  discharge module).
- Administration page only exposes the Audit Trail viewer — there are no
  user/drug/ICD-10 management CRUD endpoints yet, only read/search ones.
- LIS/RIS and ABDM integrations are functional against their own persisted
  state but do not call out to real external systems.
