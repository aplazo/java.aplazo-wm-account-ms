# java.aplazo-wm-account-ms

Microservice responsible for Walmart account linking, account lifecycle management, and account state reconciliation between Aplazo and Walmart. Handles initial linking, REAUTH_PENDING detection, and relink flows.

---

## Local development

### Prerequisites

| Tool | Version |
|------|---------|
| Docker + Docker Compose | 24+ |
| Java | 17 |
| Maven | 3.8+ |

### Option A — run-local.sh (recommended)

The script starts the docker-compose stack, waits for services to be healthy, and launches the application with the `local` profile:

```bash
./run-local.sh
```

### Option B — manual steps

**1. Start the infrastructure:**

```bash
docker compose up -d
```

Services started:

| Service | Container | Port | Description |
|---------|-----------|------|-------------|
| PostgreSQL 14 | `wm-account-ms-postgres` | `5432` | Primary database |
| LocalStack (SQS) | `wm-account-ms-localstack` | `4566` | AWS SQS emulator |

The SQS queue `wm_customer_account_changes_local` is created automatically by `scripts/localstack/init-sqs.sh` when LocalStack starts.

**2. Export AWS credentials for LocalStack** (any non-empty value works):

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
```

**3. Run the application** with the `local` profile:

```bash
# Maven
mvn spring-boot:run -pl wm-account-ms-service -Dspring-boot.run.profiles=local

# IntelliJ
# Edit Run Configuration → Add VM option: -Dspring.profiles.active=local
# Edit Run Configuration → Environment variables: AWS_ACCESS_KEY_ID=test;AWS_SECRET_ACCESS_KEY=test;AWS_DEFAULT_REGION=us-east-1
```

### Database credentials (local only)

| Property | Value |
|----------|-------|
| Host | `localhost:5432` |
| Database | `wm_account_db` |
| Username | `wm_account_user` |
| Password | `wm_account_pass` |

### Stop the infrastructure

```bash
docker compose down        # stop, keep volumes
docker compose down -v     # stop and wipe all data
```

---

## Environment variables (production)

| Variable | Description |
|----------|-------------|
| `POSTGRESQL_URL` | Full JDBC URL for PostgreSQL |
| `POSTGRES_USERNAME` | Database username |
| `POSTGRES_PASSWORD` | Database password |
| `AWS_REGION` | AWS region |
| `API_APLAZO_CUSTOMER_ACCOUNT_CHANGES_SQS_ENABLED` | Enable/disable the SQS listener (`true`/`false`) |
| `API_APLAZO_CUSTOMER_ACCOUNT_CHANGES_SQS_QUEUE_NAME` | SQS queue name for customer account events |
| `APLAZO_SDK_SECRET` | JWT secret used by the security module |
