# API reference

[Project overview](../README.md) · [Backend setup](../backend/README.md) · [Architecture](architecture.md) · [Security](security.md)

Local base URL: **http://localhost:8080**. The API retains Italian endpoint and field names; the frontend translates them into its own typed models through a [service adapter](../frontend/src/services/audioLibraryService.ts).

All request bodies below are JSON and require `Content-Type: application/json`. Except for registration, login and logout, endpoints require `Authorization: Bearer <jwtToken>`. Even a publicly visible book requires an authenticated account to access it.

## Accounts

| Method | Path                    | Request body                                       | Success                                                   |
| ------ | ----------------------- | -------------------------------------------------- | --------------------------------------------------------- |
| POST   | `/utente/registrazione` | `nome`, `cognome`, `email`, `username`, `password` | 200, account fields                                       |
| POST   | `/utente/login`         | `username` (username or email), `password`         | 200, token and account                                    |
| POST   | `/utente/logout`        | `jwtToken`                                         | 200, empty body; supplied token revoked                   |
| PATCH  | `/utente/modifica`      | `email`, `password`                                | 200, account fields; empty strings leave values unchanged |

Registration and login are separate operations. Account responses expose `nome`, `cognome` and `username`. Passwords are never included.

For a disposable local evaluation account:

```bash
curl --request POST http://localhost:8080/utente/registrazione \
  --header 'Content-Type: application/json' \
  --data '{"nome":"Demo","cognome":"Reader","email":"reader@example.com","username":"demo.reader","password":"DemoReader1!"}'

curl --request POST http://localhost:8080/utente/login \
  --header 'Content-Type: application/json' \
  --data '{"username":"demo.reader","password":"DemoReader1!"}'
```

Login returns this shape; the token below is a placeholder:

```json
{
  "jwtToken": "<returned JWT>",
  "utente": {
    "nome": "Demo",
    "cognome": "Reader",
    "username": "demo.reader"
  }
}
```

Use the actual returned token in subsequent requests. Tokens expire after five hours and are checked against persisted active-session records. Logout revokes the token in its body; an unknown or already revoked token returns 400.

## Catalog and uploads

| Method | Path                    | Request body                                         | Success                                 |
| ------ | ----------------------- | ---------------------------------------------------- | --------------------------------------- |
| POST   | `/audiolibro/lista`     | `tipo`: `1` owned, `2` public/shared, `3` favorites  | 200, audiobook array                    |
| POST   | `/audiolibro/ricerca`   | `tipo` plus `titolo` and/or ISO `dataInserimento`    | 200, matching audiobook array           |
| POST   | `/audiolibro/inserisci` | Fields described below                               | 201, created audiobook                  |
| PATCH  | `/audiolibro/modifica`  | `idAudiolibro`, `titolo`, `descrizione`, `copertina` | 200, updated audiobook; owner only      |
| POST   | `/audiolibro/rimuovi`   | `idAudiolibro`                                       | 200, soft-deleted audiobook; owner only |

API title search uses exact matching. The web interface's title/author filtering operates on the loaded catalog and is a separate behavior.

Upload fields:

| Field         | Requirement                                                                            |
| ------------- | -------------------------------------------------------------------------------------- |
| `titolo`      | Required, nonblank, at most 255 characters                                             |
| `descrizione` | Required; may be empty; at most 5,000 characters                                       |
| `copertina`   | Required raw base64 image string; may be empty; at most 2,000,000 characters           |
| `audio`       | Required, nonempty raw base64 string; at most 27,962,028 characters                    |
| `autore`      | Optional, defaults to an empty string; at most 255 characters                          |
| `durata`      | Optional, defaults to `0`; nonnegative integer seconds                                 |
| `mimeType`    | Optional, defaults to `audio/mpeg`; must match an audio media type such as `audio/wav` |

Base64 fields must not include a `data:` URL prefix. The frontend accepts files up to **20 MiB** and detects duration when the user does not supply it. The backend stores the supplied duration; MIME validation does not inspect the actual audio encoding.

Request DTOs are the source of truth for field validation: [request models](../backend/src/main/java/com/ingswpf/audiolibrarybe/dto/request).

## Media and progress

The frontend uses these variants to keep encoded audio out of routine catalog and listening updates:

| Request                                      | Response                                                                                 |
| -------------------------------------------- | ---------------------------------------------------------------------------------------- |
| `POST /audiolibro/lista?metadataOnly=true`   | 200, catalog with `audio` and `copertina` set to `null`                                  |
| `GET /audiolibro/{id}/audio`                 | Binary media with its audio content type; bearer authentication and book access required |
| `POST /audiolibro/ascolta?metadataOnly=true` | 204, no response body after saving listening progress                                    |
| `POST /audiolibro/ascolta`                   | 200, audiobook response after saving listening progress                                  |

Omitting `metadataOnly` preserves the original response contract, including base64 media fields. An audiobook response includes `idAudiolibro`, `titolo`, `autore`, `durata`, `mimeType`, `descrizione`, `audio`, `copertina`, `pubblico`, `preferito`, `dataInserimento`, `creatore` and `ultimoAscolto`. Listening records contain `secondi`, `data` and `updatedAt`. Without a listening record, `ultimoAscolto` contains `secondi: 0` with `data` and `updatedAt` set to `null`.

To request the current user's owned catalog, replace the token placeholder:

```bash
curl --request POST 'http://localhost:8080/audiolibro/lista?metadataOnly=true' \
  --header 'Authorization: Bearer <returned JWT>' \
  --header 'Content-Type: application/json' \
  --data '{"tipo":1}'
```

Save progress with a real accessible book ID from that catalog. For example, the JSON body below saves second 42 for book 1:

```json
{
  "idAudiolibro": 1,
  "secondi": 42
}
```

`secondi` is a nonnegative integer. Progress belongs to the authenticated user and selected book. Saving one book's position does not overwrite another's record.

## Favorites, visibility and sharing

| Method | Path                              | Request body                                          | Access          |
| ------ | --------------------------------- | ----------------------------------------------------- | --------------- |
| PATCH  | `/audiolibro/aggiungiAiPreferiti` | `idAudiolibro`                                        | Accessible book |
| PATCH  | `/audiolibro/rimuoviDaiPreferiti` | `idAudiolibro`                                        | Accessible book |
| PATCH  | `/audiolibro/rendiPubblico`       | `idAudiolibro`                                        | Owner           |
| PATCH  | `/audiolibro/rendiNonPubblico`    | `idAudiolibro`                                        | Owner           |
| POST   | `/audiolibro/condividi`           | `idAudiolibro`, `utenti: [{"username": "recipient"}]` | Owner           |
| POST   | `/audiolibro/rimuoviCondivisioni` | `idAudiolibro`, `utenti: [{"username": "recipient"}]` | Owner           |

Successful operations return 200 with the audiobook representation. Multi-recipient operations are transactional: an invalid recipient prevents the whole change from being committed. Visibility, favorites and sharing follow the [service rules](../backend/src/main/java/com/ingswpf/audiolibrarybe/service/AudiolibroService.java).

These management capabilities are available through the API; the current web interface does not expose all of them.

## Error handling

| Status | Typical meaning                                                                       |
| ------ | ------------------------------------------------------------------------------------- |
| 400    | Invalid input, malformed JSON or a rejected operation such as repeated sharing        |
| 401    | Missing, invalid, expired or revoked bearer token; invalid login credentials          |
| 403    | Request rejected by the CORS origin policy                                            |
| 404    | Unavailable book or recipient; ownership-only operations also hide inaccessible books |
| 415    | Unsupported request content type                                                      |
| 500    | Unexpected server failure; exception details are not returned                         |

Error responses may contain plain text or an empty body; clients must not assume a uniform JSON error envelope. See the [controllers](../backend/src/main/java/com/ingswpf/audiolibrarybe/controller) for operation-specific responses and the [integration suite](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java) for executable request examples.
