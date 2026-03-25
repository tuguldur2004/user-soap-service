# user-soap-service-spring

SOAP authentication service

## Features

- `RegisterUser`
- `LoginUser`
- `ValidateToken`

## Run

```bash
mvn spring-boot:run
```

Runs on `http://localhost:8080/ws` by default.

The app now binds using `server.port=${PORT:8080}` so PaaS providers (including DigitalOcean App Platform) can inject runtime `PORT` automatically.

## Run with Docker (Option A)

Build image:

```bash
docker build -t user-soap-service-spring .
```

Run container locally:

```bash
docker run --rm -p 8080:8080 \
	-e AUTH_DB_URL="jdbc:postgresql://host.docker.internal:5432/auth_db" \
	-e AUTH_DB_USER="postgres" \
	-e AUTH_DB_PASSWORD="your_postgres_password" \
	-e APP_JWT_SECRET="replace-with-a-long-random-secret" \
	user-soap-service-spring
```

For DigitalOcean App Platform, keep environment variables in App settings (not `.env` files).

### Windows PowerShell example

```powershell
$env:AUTH_DB_URL="jdbc:postgresql://localhost:5432/auth_db"
$env:AUTH_DB_USER="postgres"
$env:AUTH_DB_PASSWORD="your_postgres_password"
$env:APP_JWT_SECRET="replace-with-a-long-random-secret"
mvn spring-boot:run
```

## Environment

- `AUTH_DB_URL`
- `AUTH_DB_USER`
- `AUTH_DB_PASSWORD` (required)
- `APP_JWT_SECRET`
