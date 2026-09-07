# AudioLibrary

**A web app for listening to audiobooks and managing your personal audiobook collection.**

AudioLibrary lets you upload audiobooks, browse and search your library, listen directly in the browser and resume each audiobook where you left off. Built with React, TypeScript and Java 17, it includes account-based access and persistent listening progress.

Developed by **Antonio Baldari** · [Portfolio](https://baldari.dev) · [MIT license](LICENSE)

[Architecture](docs/architecture.md) · [Engineering case study](docs/review.md) · [API reference](docs/api.md) · [Security](docs/security.md)

## What you can do

- **Build your audiobook library.** Upload audiobooks with a title, author and description. Duration is detected from the file or entered manually.
- **Find and listen to an audiobook.** Search by title or author and use native audio controls with keyboard support.
- **Continue listening.** Positions are saved per audiobook and per account, with remote synchronization in connected mode.
- **Choose how to try it.** Run a device-local browser demo or the complete application with registration, login and a persistent local database.

The API also supports favorites, public/private visibility, sharing, metadata edits and soft deletion. The current web interface focuses on accounts, uploading, browsing and playback.

## Engineering highlights

| Area                 | Implementation                                                                                |
| -------------------- | --------------------------------------------------------------------------------------------- |
| Frontend structure   | React components, typed Redux state, lifecycle hooks and a dedicated API adapter              |
| Backend design       | Spring Boot controllers, transactional services, Spring Data repositories and DTO mappers     |
| Media handling       | IndexedDB for demo uploads; metadata-only catalog responses and authenticated media retrieval |
| Playback consistency | Hydration before persistence, per-track positions and serialized remote writes                |
| Access control       | BCrypt passwords, signed JWTs, persisted session revocation and ownership checks              |
| Verification         | Component, state, service, security and database integration tests; separate CI jobs          |

The [case study](docs/review.md) connects these decisions to concrete failure cases and regression tests.

## Run the browser demo

Use **Node.js 24** and npm. Run these commands from the repository root:

```bash
npm ci
npm start
```

Open **http://127.0.0.1:3000**. With `VITE_API_BASE_URL` unset or empty, uploads remain in this browser's IndexedDB. A short external audio sample lets you try the player immediately.

To explore the main flow:

1. Upload an audio file of up to **20 MiB** and enter its title and author.
2. Search for it, select **Play**, then pause partway through.
3. Reload the page and resume from the saved position.

**Space** toggles playback and **F** focuses the player when focus is outside an interactive control. Browser demo data stays on this device.

## Run with the backend

Install **Java 17** in addition to the frontend prerequisites. The repository includes a Maven wrapper.

After `npm ci`, start the backend in a separate terminal:

```bash
npm run start:backend
```

This uses the explicit `dev` profile: the API runs at **http://localhost:8080** and persists an H2 database in `backend/.data/`.

Create `frontend/.env.local` with:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

Start or restart `npm start`, open **http://localhost:3000**, and create an account. Use this exact frontend origin with the default CORS configuration. Your connected library includes your own books and books made public or shared with you.

See the [frontend guide](frontend/README.md) and [backend guide](backend/README.md) for configuration, commands and troubleshooting.

## Verify the project

From the repository root:

```bash
npm run lint
npm run prettier:check
npm test
npm run build
npm run test:backend
```

**Local verification on 7 September 2026:** 26 frontend tests and 65 backend tests passed, along with lint, formatting checks and both builds. These are recorded results, not a live CI status.

The [GitHub Actions workflow](.github/workflows/ci.yml) defines frontend and backend checks for pushes and pull requests. Backend integration tests use H2 and exercise real authentication and database writes. [Test scope and evidence](docs/review.md#verification).

## Repository map

```text
frontend/             React application, Vite configuration and frontend tests
backend/              Spring Boot application, Maven wrapper and backend tests
docs/                 Architecture, engineering decisions, API and security
.github/workflows/    Frontend and backend CI
```

| Read next                                | What it covers                                                    |
| ---------------------------------------- | ----------------------------------------------------------------- |
| [Architecture](docs/architecture.md)     | Component boundaries, data flow and design patterns               |
| [Engineering case study](docs/review.md) | Reliability problems, implementation choices and evidence         |
| [API reference](docs/api.md)             | Authentication, request examples, endpoints and responses         |
| [Security](docs/security.md)             | XSS, CSRF, SQL injection controls and deployment responsibilities |
| [Frontend guide](frontend/README.md)     | Browser modes, source entry points and build configuration        |
| [Backend guide](backend/README.md)       | Java setup, profiles, environment variables and database scope    |

## Current scope

AudioLibrary is designed for modest personal libraries. Uploads use base64 JSON, media is stored in the database, and the frontend downloads the selected file before playback. Larger collections would benefit from pagination and separate media storage.

H2 is the verified local evaluation environment. A MySQL deployment needs a reviewed schema migration and database-specific testing. Public hosting also needs HTTPS, frontend security headers and deployment configuration. These boundaries and the next engineering steps are documented in the [architecture](docs/architecture.md#extension-points) and [security guide](docs/security.md#deployment-checks).

## Author and license

**Antonio Baldari** · [baldari.dev](https://baldari.dev)

Distributed under the [MIT license](LICENSE).
