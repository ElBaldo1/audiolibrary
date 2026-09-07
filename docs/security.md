# Application security

[Project overview](../README.md) · [Architecture](architecture.md) · [API reference](api.md) · [Test evidence](review.md#verification)

AudioLibrary's security model combines explicit bearer authentication, server-side access rules, safe text rendering and parameter-bound database queries. This guide describes the implemented controls, the tests supporting them and the deployment configuration they depend on.

## Authentication and authorization

Passwords are hashed with BCrypt. Login issues an HS512-signed JWT with a unique identifier and a five-hour lifetime. Requests require both a valid signed token and its persisted active-session record; logout revokes that record.

Read access and ownership are separate checks. Users can access their own books and books that are public or shared with them. Editing, deleting, changing visibility and managing shares require ownership. Unavailable books return 404, including at the authenticated media endpoint.

The frontend stores its token in `sessionStorage`, scoped to the API URL, and adds it explicitly to requests. JavaScript running through an XSS flaw could read this token. Browser storage, CSP and token expiry do not eliminate that risk.

**Implementation:** [Spring security configuration](../backend/src/main/java/com/ingswpf/audiolibrarybe/security/WebSecurityConfig.java), [JWT utilities](../backend/src/main/java/com/ingswpf/audiolibrarybe/security/JwtTokenUtil.java) and [frontend API client](../frontend/src/services/apiClient.ts).

## XSS prevention

User-provided titles, authors, account fields and errors are rendered as React text. The interface does not insert user content as HTML or evaluate it as code. Characters such as apostrophes and angle brackets remain valid data. Regression tests verify that malicious markup remains visible text without creating executable DOM elements.

At the player boundary, [media URL validation](../frontend/src/utils/audioSource.ts) accepts HTTP(S) URLs without embedded credentials, audio data URLs and the specific authenticated audio route. JavaScript URLs, HTML/SVG data URLs and ambiguous relative paths are rejected. The player also creates and releases its own Blob URLs for retrieved files.

Production builds include a Content Security Policy before scripts load:

| Area                                                          | Policy behavior                                                         |
| ------------------------------------------------------------- | ----------------------------------------------------------------------- |
| Scripts                                                       | Same-origin scripts; no inline scripts or `eval`                        |
| API requests                                                  | Frontend origin and the configured API origin                           |
| Media                                                         | Same-origin assets, local Blob/data sources and `https://samplelib.com` |
| Embedded objects, base URL changes and native form navigation | Disabled                                                                |
| Styles                                                        | Same-origin and inline styles for Bootstrap/Toastify compatibility      |

The production policy is generated in [vite.config.ts](../frontend/vite.config.ts). Development omits it to support Vite tooling. The separately hosted API sends its own restrictive CSP and `X-Content-Type-Options: nosniff`; these API headers do not protect the frontend HTML document.

The backend validates declared upload media types and input sizes. This does not prove that a file contains valid audio, inspect its codec or scan its contents for malware.

## CSRF and credential transport

The API authenticates through an explicitly supplied `Authorization: Bearer` header. Cookie authentication, HTTP Basic, Spring form login and Spring's automatic logout endpoint are disabled. The application provides its own JSON logout operation. Login returns JSON without setting an authentication cookie, and frontend requests use `credentials: 'omit'`.

Spring's CSRF-token filter is intentionally disabled under this contract: a forged cross-site request cannot make the browser automatically attach the victim's bearer token. State-changing endpoints require JSON, GET endpoints do not change business data, and CORS permits configured origins without cookie credentials. Stateless session configuration and CORS alone would not establish this protection.

If authentication changes to HttpOnly cookies, HTTP Basic or another browser-attached credential, enable CSRF-token validation and implement the frontend token exchange before enabling that transport. Cookie attributes such as `Secure`, `HttpOnly` and `SameSite` provide additional protection; a token stored only in a cookie is not sufficient CSRF validation.

Regression tests verify that cookie/Basic credentials do not authenticate, explicit bearer credentials work, hostile origins are rejected, browser form content types cannot log in, and login issues no authentication cookie.

## SQL injection prevention

Repositories use Spring Data derived queries and parameter-bound JPQL. Request values are not concatenated into SQL or JPQL strings. IDs are typed and validated; the existing search strategies compare data without generating query strings.

Integration tests submit SQL payloads through login and logout lookup fields and verify that they cannot authenticate or revoke another session. Titles containing apostrophes, SQL syntax and markup round-trip as literal data without changing the schema.

These tests use H2. They support the query review but do not establish behavior for every MySQL deployment. Future custom queries must bind values as parameters. Dynamic identifiers or sort directions need a fixed allowlist because they cannot generally be bound as query values.

## Regression evidence

| Concern                                                                   | Evidence                                                                                                                 |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Safe text, media URLs and omitted browser credentials                     | [Frontend security tests](../frontend/src/tests/security.test.tsx)                                                       |
| Authentication transport, origins, SQL payloads and literal stored markup | [Backend integration tests](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java) |
| Ownership, relationship scope and transaction rollback                    | [Backend integration tests](../backend/src/test/java/com/ingswpf/audiolibrarybe/integration/LibraryIntegrationTest.java) |

Run the [verification commands](../README.md#verify-the-project) to reproduce the automated checks. The [case study](review.md#verification) records the last local results and their scope. The suite covers specific regressions; it is not an exhaustive penetration test.

## Deployment checks

Before exposing the application publicly, configure and verify these controls in the actual hosting environment:

- **Transport and secrets.** Serve the frontend and API over HTTPS. Supply a high-entropy `JWT_SECRET` and database credentials through the runtime environment; use the default profile with a prepared database rather than the local `dev` profile.
- **Browser origins.** Set `ALLOWED_ORIGINS` to the deployed frontend origins and rebuild the frontend with its actual `VITE_API_BASE_URL`.
- **Frontend HTTP headers.** Retain the generated CSP and configure `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer` and CSP `frame-ancestors 'none'` on the static host. The framing directive must be an HTTP header because CSP meta elements do not support it.
- **Database operations.** Apply a reviewed schema migration, run MySQL-specific tests and grant the application account only the privileges it needs.
- **Abuse and resource limits.** Add request rate limits, request-body limits and timeouts appropriate to the base64 upload contract. Review expired-session cleanup and account recovery before wider use.
- **Release validation.** Review dependency updates and framework support, then check deployed headers, CORS and playback in a browser.

These are deployment responsibilities and extension work, not capabilities established by the local test suite. See the [backend configuration guide](../backend/README.md#configuration-and-profiles) and [architecture extension points](architecture.md#extension-points).

## Further reading

- [OWASP: Cross-Site Scripting Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [Spring Security: Cross Site Request Forgery](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html)
- [OWASP: SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
