# CapRover Deployment

This repository is deployed to CapRover as four separate apps:

- `project-cv-db`
- `project-cv-pdf`
- `project-cv-backend`
- `project-cv-frontend`

The frontend serves the SPA and proxies `/api` plus `/uploads` to the backend through `frontend/nginx.conf`. The backend therefore does not need to be exposed through HTTPS for normal browser traffic.

## Files

- `.env.caprover.example`: environment template
- `scripts/caprover/bootstrap.sh`: one-time app bootstrap
- `scripts/caprover/deploy.sh`: repeatable service deploy
- `scripts/caprover/verify.sh`: post-deploy smoke checks

## Required Secrets

Copy `.env.caprover.example` to `.env.caprover.local` and fill in:

- `CAPROVER_URL`
- `CAPROVER_PASSWORD`
- `CAPROVER_POSTGRES_PASSWORD`
- `CAPROVER_JWT_SECRET`

Optional overrides exist for app names and database name, but the defaults match the current production setup.

## One-Time Bootstrap

Use this when the CapRover instance is empty or the apps do not exist yet:

```bash
chmod +x scripts/caprover/*.sh
./scripts/caprover/bootstrap.sh
```

What bootstrap does:

- creates the four apps if they are missing
- applies the correct `containerHttpPort` and env vars
- deploys PostgreSQL as `postgres:16-alpine` with a persistent volume
- enables base-domain SSL for the frontend app

Bootstrap only submits the database deploy. Run the normal deploy script after that.

## Repeatable Deploy

For code changes, use:

```bash
./scripts/caprover/deploy.sh
```

What deploy does:

- re-syncs frontend, backend, and pdf-renderer app settings
- packages `backend/`, `frontend/`, and `services/pdf-renderer/` into separate tarballs
- uploads each tarball to its matching CapRover app

The uploads are submitted in detached mode, so CapRover continues building after the command exits.

## Verify

After CapRover finishes building, run:

```bash
./scripts/caprover/verify.sh
```

Current expected endpoints:

- Backend health: `http://project-cv-backend.<root-domain>/actuator/health`
- Frontend API proxy: `https://project-cv-frontend.<root-domain>/api/v1/templates`

## Current Production Mapping

On the current server, the scripts resolve to:

- Frontend: `https://project-cv-frontend.rnsj.913555.xyz`
- Backend health: `http://project-cv-backend.rnsj.913555.xyz/actuator/health`
- PDF renderer internal URL: `http://srv-captain--project-cv-pdf:3100`
- PostgreSQL internal URL: `jdbc:postgresql://srv-captain--project-cv-db:5432/project_cv`

## Notes

- `docker-compose.caprover.yml` and the root `captain-definition` are older experiments and are not the active production path.
- The deploy scripts use the CapRover HTTP API directly, so they do not depend on a pre-saved local CapRover login session.
- If `verify.sh` fails right after deploy, check the app runtime logs in CapRover first; detached builds may still be running.
