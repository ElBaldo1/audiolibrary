# Engineering case study

[Project overview](../README.md) · [Architecture](architecture.md) · [API](api.md) · [Security](security.md)

AudioLibrary combines a React listening interface with a Java backend in one runnable repository. The engineering work centers on keeping the API contract, playback state and access rules consistent across that boundary.

## 1. Connect the interface to the actual API

**Problem.** The frontend's expected endpoints differed from the supplied backend contract. Remote upload failures could also appear as successful local saves.

**Decision.** Introduce an explicit adapter around the existing Java endpoints and retain separate browser-demo and connected modes. Registration and login establish the account context; connected requests propagate failures to the interface.

**Result.** A failed remote upload preserves form input and reports an error. Catalog DTOs are translated once into frontend models, and duplicate entries from owned/shared lists are consolidated.

**Evidence:** [catalog service tests](../frontend/src/tests/audioLibraryService.test.ts) and [backend integration tests](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java).

## 2. Make playback restoration predictable

**Problem.** Loading saved progress and mounting the audio element happen asynchronously. An early zero-position event could overwrite the saved position, while switching stories could carry progress across tracks.

**Decision.** Hydrate application state before persistence begins, restore the media position after metadata arrives, and keep positions by account and track. Serialize remote updates through a five-second synchronization interval with pause/visibility flushes.

**Result.** Restoring a session and selecting another story have explicit state transitions. Remote writes avoid a request for every media time-update event. The per-client queue controls ordering; concurrent-device conflict resolution remains a separate concern.

**Evidence:** [player regression tests](../frontend/src/tests/AudioPlayer.test.tsx), [progress service tests](../frontend/src/tests/playbackService.test.ts) and [StrictMode application test](../frontend/src/tests/App.test.tsx).

## 3. Keep binary data out of routine state requests

**Problem.** Browser uploads could exceed localStorage's practical capacity. Catalog and progress responses also carried encoded audio when only metadata or an acknowledgement was needed.

**Decision.** Store demo audio in IndexedDB. Add metadata-only catalog responses, an authenticated media endpoint and a `204 No Content` response for metadata-only progress writes.

**Result.** Demo audio uses asynchronous storage, and routine catalog/progress traffic avoids redundant media payloads. A regression test persists an audio representation larger than 6 MiB in IndexedDB. The selected remote file is still downloaded in full, so streaming and separate media storage remain future work.

**Evidence:** [IndexedDB regression test](../frontend/src/tests/audioLibraryService.test.ts), [media and progress API](api.md#media-and-progress) and [integration tests](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java).

## 4. Preserve ownership and relationship integrity

**Problem.** Read access to a public/shared book could be confused with permission to edit it. Relationship changes could remove unrelated favorites or leave a sharing operation partly applied.

**Decision.** Require ownership for edits, update only the affected relationships, and execute audiobook mutations transactionally. Correct entity identity handling and keep listening records attached to their respective book/user pair.

**Result.** Changing one book's visibility preserves unrelated shares and favorites. A sharing request with an invalid recipient rolls back as a whole. Repeated listening updates retain independent positions for different books.

**Evidence:** [real-persistence integration tests](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java), including ownership, revocation scope and rollback cases.

## 5. Make security assumptions testable

The API accepts explicit bearer credentials, with cookie and Basic authentication disabled. Frontend requests omit cookies. Rendering treats user content as text, the production build includes a CSP, and repository queries bind input as parameters.

Tests exercise malicious markup, unsupported media URLs, SQL payloads, untrusted origins and attempted cookie-based authentication. The [security guide](security.md) explains what these controls establish and which deployment checks remain necessary.

## Verification

The last local verification on **7 September 2026** produced these results:

| Check                     | Recorded result | Scope                                                            |
| ------------------------- | --------------- | ---------------------------------------------------------------- |
| Frontend tests            | 26 passed       | Components, services, Redux, StrictMode restoration and security |
| Backend controller tests  | 53 passed       | HTTP contracts and validation with mocked collaborators          |
| Backend integration tests | 12 passed       | Real security filters, services, repositories and H2 persistence |
| Frontend build            | Passed          | Strict TypeScript check and Vite production output               |
| Backend build             | Passed          | Maven verification and executable JAR packaging                  |
| Lint and formatting       | Passed          | Frontend source checks                                           |
| CSP build inspection      | Passed          | Generated demo and connected-mode policies                       |

These are dated local results. The [CI workflow](../.github/workflows/ci.yml) defines the repeatable checks; this document does not assert a remote CI run or coverage percentage.

The backend was also started locally and checked for allowed-origin CORS handling and rejection of unauthenticated requests. Browser visual review, assistive-technology testing, MySQL verification and production load testing are outside the recorded validation.

To reproduce the automated checks, run the commands in the [project README](../README.md#verify-the-project).
