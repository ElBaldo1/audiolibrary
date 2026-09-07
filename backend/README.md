# AudioLibrary backend

[Project overview](../README.md) · [Architecture](../docs/architecture.md) · [API reference](../docs/api.md) · [Security](../docs/security.md)

The Java API owns accounts, library visibility, sharing and listening progress. Spring Boot controllers validate requests, transactional services enforce business rules, and Spring Data JPA repositories persist the resulting state.

## Start locally

Install **Java 17**; Eclipse Temurin is used in CI. Maven is provided through the repository wrapper. From the repository root:

```bash
npm run start:backend
```

To run without Node.js, execute these commands from the repository root instead:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

The API listens on **http://localhost:8080**. The `dev` profile creates and updates an H2 file database at `backend/.data/audiolibrary` when launched as above. Data survives restarts. Register through the connected frontend or the [account API](../docs/api.md#accounts); no seed account is required.

The development signing key and database settings are for local evaluation. The `dev` profile is not a public deployment configuration.

## Configuration and profiles

| Profile | Database                                  | Schema behavior         | Purpose                              |
| ------- | ----------------------------------------- | ----------------------- | ------------------------------------ |
| `dev`   | Local H2 file in MySQL compatibility mode | Hibernate `update`      | Persistent local evaluation          |
| `test`  | In-memory H2                              | Hibernate `create-drop` | Isolated automated integration tests |
| Default | Configured MySQL connection               | Hibernate `validate`    | Existing, prepared database schema   |

The default profile reads these process environment variables from [application.properties](src/main/resources/application.properties):

| Variable          | Default or requirement                                                |
| ----------------- | --------------------------------------------------------------------- |
| `DB_URL`          | Defaults to `jdbc:mysql://localhost:3306/audiolibrary`                |
| `DB_USERNAME`     | Defaults to `audiolibrary`                                            |
| `DB_PASSWORD`     | Required; no committed default                                        |
| `JWT_SECRET`      | Required; a high-entropy signing secret of at least 64 UTF-8 bytes    |
| `ALLOWED_ORIGINS` | Comma-separated frontend origins; defaults to `http://localhost:3000` |

`JWT_SECRET` is consumed as UTF-8 text, without base64 decoding. Supply real credentials through the runtime environment or a secret manager. The backend does not automatically load a frontend-style `.env` file.

Origins include the scheme, hostname and port. For example, evaluating from both local hostnames requires `ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000` in the backend process environment.

## Build and test

From the repository root:

```bash
npm run test:backend
```

Or from `backend/`, without npm:

```bash
./mvnw -B -ntp verify
```

Verification runs tests and packages `target/audiolibrary-be-0.0.1-SNAPSHOT.jar`. With the required environment variables and a prepared MySQL schema, launch that artifact from `backend/` using:

```bash
java -jar target/audiolibrary-be-0.0.1-SNAPSHOT.jar
```

The [controller tests](src/test/java/com/ingswpf/audiolibrarybe/controller) validate HTTP contracts with mocked collaborators. The [integration suite](src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java) exercises real filters, services and H2 persistence, including ownership checks, transaction rollback and session revocation. See the [recorded verification results](../docs/review.md#verification).

## Where to read the code

All application packages are under `src/main/java/com/ingswpf/audiolibrarybe/`.

| Package                                                           | Responsibility                                    |
| ----------------------------------------------------------------- | ------------------------------------------------- |
| [controller](src/main/java/com/ingswpf/audiolibrarybe/controller) | JSON endpoints, validation and response mapping   |
| [service](src/main/java/com/ingswpf/audiolibrarybe/service)       | Account, access, relationship and listening rules |
| [repository](src/main/java/com/ingswpf/audiolibrarybe/repository) | Spring Data queries and persistence               |
| [model](src/main/java/com/ingswpf/audiolibrarybe/model)           | Entities and relationship identity                |
| [dto](src/main/java/com/ingswpf/audiolibrarybe/dto)               | Request/response contracts and mappers            |
| [security](src/main/java/com/ingswpf/audiolibrarybe/security)     | Bearer authentication, signing, CORS and headers  |
| [strategy](src/main/java/com/ingswpf/audiolibrarybe/strategy)     | Catalog selection and search variants             |

## MySQL deployment scope

The repository supplies MySQL connection configuration but does not yet include versioned schema migrations. The default profile validates a schema; it does not create one. Review entity changes, create a migration and run database-specific tests before using an existing MySQL database.

Schema changes include audiobook author, duration and media type, timestamped listening progress, unique account identifiers and storage for longer JWT values. H2 compatibility mode does not establish equivalent MySQL behavior.

Audio is stored as binary database content and uploaded through base64 JSON. The [architecture extension points](../docs/architecture.md#extension-points) cover pagination, media storage and further account lifecycle work.

## Troubleshooting

| Symptom                               | Check                                                                                             |
| ------------------------------------- | ------------------------------------------------------------------------------------------------- |
| Java version or compilation error     | Run `java -version` and `./mvnw -version`; both should use Java 17                                |
| Missing `DB_PASSWORD` or `JWT_SECRET` | Select `dev` for local evaluation, or supply the default profile's required environment variables |
| Schema validation fails               | Check the database schema against the entities; apply a reviewed migration                        |
| Browser requests receive a CORS error | Match the browser's exact origin in `ALLOWED_ORIGINS`, then restart the backend                   |
| Protected requests return 401         | Log in again and send the returned token as `Authorization: Bearer <token>`                       |
| H2 reports a file lock                | Check for another backend process using the same local database                                   |

Endpoint examples are in the [API reference](../docs/api.md). Deployment security responsibilities are in the [security guide](../docs/security.md#deployment-checks).
