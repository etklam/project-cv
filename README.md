# project-cv

Monorepo scaffold for a CV builder product.

## Layout

- `backend/`: Kotlin + Spring Boot API
- `frontend/`: Vue 3 + Vite web app
- `services/pdf-renderer/`: internal PDF rendering worker
- `dev-plan.md`: product and implementation plan

## Status

The repository is being scaffolded from `dev-plan.md`.

## Docker

Run the full stack, including PostgreSQL:

```bash
docker compose up --build -d
docker compose ps
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- PDF renderer: `http://localhost:3100/health`

## Flyway

Fresh databases bootstrap through `backend/src/main/resources/db/migration/B11__baseline_schema.sql`.
Existing environments keep their historical `V1` to `V11` records unchanged to avoid checksum breakage.

If you want to verify the clean bootstrap path locally, recreate the PostgreSQL volume before starting Docker again:

```bash
docker compose down -v
docker compose up --build -d
```
