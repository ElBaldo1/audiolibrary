# AudioLibrary frontend

[Project overview](../README.md) · [Architecture](../docs/architecture.md) · [Backend guide](../backend/README.md)

The React application handles account access, audio uploads, catalog search and playback. TypeScript models and a service adapter keep the interface independent of the backend's request and response names.

## Start locally

Use Node.js 24 and npm. From the repository root:

```bash
npm ci
npm start
```

The development server listens on port **3000**. Follow the [guided demo](../README.md#run-the-browser-demo) to upload a story and test playback restoration.

## Choose a data source

Create `frontend/.env.local` using [.env.example](.env.example) as a reference.

| Setting                                   | Behavior                                                                                    |
| ----------------------------------------- | ------------------------------------------------------------------------------------------- |
| `VITE_API_BASE_URL=`                      | Browser demo: catalog and uploads in IndexedDB, listening positions stored locally          |
| `VITE_API_BASE_URL=http://localhost:8080` | Connected mode: account authentication, backend catalog and remote progress synchronization |

Restart Vite after changing the setting. For connected mode, start the [backend](../backend/README.md#start-locally), open **http://localhost:3000** and register an account. The default backend CORS policy permits this exact origin; `127.0.0.1` is a different origin.

Demo and connected libraries are separate. Connecting the backend does not upload the browser's demo collection. Browser storage is origin-specific, so switching between `localhost` and `127.0.0.1` also changes the local collection you see.

`VITE_` values are public build configuration. Use an HTTP(S) API URL without embedded credentials, and keep secrets out of frontend environment files.

## Commands

Run these from the repository root; the workspace scripts select the frontend automatically.

| Command                                   | Purpose                                                           |
| ----------------------------------------- | ----------------------------------------------------------------- |
| `npm start`                               | Run Vite on port 3000                                             |
| `npm test`                                | Run the Vitest suite once                                         |
| `npm run test:watch --workspace frontend` | Watch frontend tests during development                           |
| `npm run lint`                            | Check source with ESLint, including hooks and accessibility rules |
| `npm run prettier:check`                  | Check TypeScript, TSX and CSS formatting                          |
| `npm run format`                          | Format those source files                                         |
| `npm run build`                           | Check TypeScript and generate `frontend/dist/`                    |

The root [lockfile](../package-lock.json) is shared by the npm workspace. Install dependencies from the root with `npm ci` for a reproducible checkout.

## Where to read the code

| Entry point                                                     | Responsibility                                                    |
| --------------------------------------------------------------- | ----------------------------------------------------------------- |
| [App.tsx](src/App.tsx)                                          | Compose account, catalog and player flows; handle shortcuts       |
| [AudioPlayer.tsx](src/components/audio/AudioPlayer.tsx)         | Native playback controls and media lifecycle                      |
| [AudioUploadForm.tsx](src/components/audio/AudioUploadForm.tsx) | Validate uploads, detect duration and retain input after failures |
| [usePlaybackSync.ts](src/hooks/usePlaybackSync.ts)              | Hydrate and synchronize listening state                           |
| [audioLibraryService.ts](src/services/audioLibraryService.ts)   | Adapt backend DTOs and select the catalog data source             |
| [apiClient.ts](src/services/apiClient.ts)                       | Authenticated JSON requests and session handling                  |
| [localLibrary.ts](src/services/localLibrary.ts)                 | IndexedDB catalog persistence                                     |
| [playbackService.ts](src/services/playbackService.ts)           | Account/track storage keys and remote progress writes             |
| [tests](src/tests)                                              | Component, state, service and security regressions                |

The [architecture guide](../docs/architecture.md#playback-consistency) explains hydration and write ordering. The [case study](../docs/review.md) links the implementation to its regression tests.

## Production build

Set `VITE_API_BASE_URL` before running `npm run build`: Vite embeds the configuration into the static output. Changing the API URL after deployment requires a rebuild.

[vite.config.ts](vite.config.ts) adds a production Content Security Policy to `index.html`, including the configured API origin. The hosting service must supply HTTPS and the additional HTTP headers described in [deployment checks](../docs/security.md#deployment-checks).

## Troubleshooting

| Symptom                                       | Check                                                                                                 |
| --------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Accounts are not shown                        | Set `VITE_API_BASE_URL` and restart Vite                                                              |
| Login or catalog requests fail                | Confirm the API is running and the frontend origin matches `ALLOWED_ORIGINS`                          |
| A previously saved demo collection is missing | Use the same browser profile and origin; check whether browser storage was cleared                    |
| An uploaded file cannot play                  | Use a browser-supported audio encoding; an `audio/*` MIME type alone does not establish codec support |
| The sample does not play offline              | Upload a local file; the bundled sample points to an external host                                    |
| Port 3000 is occupied                         | Stop the conflicting development server; Vite uses a strict port to keep CORS predictable             |

See [Security](../docs/security.md) for safe rendering, media URL validation and the bearer-token storage tradeoff.
