# LEVIA - Phase 2: Authentication (progress report)

Status: **code complete, NOT yet built or run end-to-end.** See "Tests" for exactly what was verified.

## What Phase 1 already had (kept unchanged)
Register, login (no email verification), refresh-token rotation with reuse detection, logout, logout-all,
change password, forgot/reset password, JWT access tokens with `token_version` revocation.

## What Phase 2 added

### Backend (`src/`)
| File | Status |
| --- | --- |
| `validators/accountValidators.js` | new (FreeLLMAPI draft, reviewed) |
| `services/accountService.js` | new (FreeLLMAPI draft, reviewed + minor hardening) |
| `controllers/accountController.js` | new (FreeLLMAPI draft, reviewed) |
| `routes/accountRoutes.js` | new (FreeLLMAPI draft, reviewed) |
| `services/authService.js` | changed: now also exports `revokeEverywhere` |
| `routes/index.js` | changed: mounts `/account` |

New endpoints (all need `Authorization: Bearer <access token>`):

| Method | Path | Body | Result |
| --- | --- | --- | --- |
| GET | `/api/v1/account/sessions` | header `x-refresh-token` (optional, marks the current session) | `{ sessions: [{ id, deviceInfo, startedAt, lastActiveAt, isCurrent }] }` |
| DELETE | `/api/v1/account/sessions/:familyId` | - | `{ revoked: true }` (404 if not an active session of yours) |
| PATCH | `/api/v1/account/email` | `{ password, newEmail }` | `{ email }` (409 if taken; no verification, by product rule) |
| POST | `/api/v1/account/deactivate` | `{ password }` | `{ deactivated: true }`; logging in again reactivates |
| POST | `/api/v1/account/delete` | `{ password }` | `{ deleted: true }`; anonymizes the user, frees email + username, hides posts, revokes all sessions |

Notes: a revoked session's *access* token can live up to 15 minutes (its lifetime). Deactivate, delete, change password and
log-out-all bump `token_version`, so those take effect immediately.

### Android (`android/app/src/main/`)
- New: `ui/ForgotPasswordActivity.java` (FreeLLMAPI/Mistral draft + 2 fixes), `ui/AccountSecurityActivity.java`, `ui/AuthFlow.java`,
  8 Retrofit model classes, layouts `activity_forgot_password.xml`, `activity_account_security.xml`, `item_session.xml`,
  `drawable/bg_card.xml`, `values/strings_forgot_password.xml`, `values/strings_account_security.xml`.
- Changed: `ApiService.java` (9 new calls), `ApiClient.java` (a wrong-password 401 no longer triggers a token refresh),
  `LoginActivity` + `activity_login.xml` ("Forgot password?" link), `MainActivity` + `activity_main.xml` (Account security button), `AndroidManifest.xml`.
- No Gradle, plugin, or dependency changes (still AGP 4.0.2, Gradle 6.1.1, Java 8).

Password reset flow: the email contains the reset link and a **reset code**; the app screen takes the code + new password.
Without SMTP configured, the link/code is printed to the server log only when `NODE_ENV` is not `production`.
**To receive real reset emails on Render you must set `SMTP_HOST/PORT/USER/PASS/MAIL_FROM`.**

## FreeLLMAPI usage this phase
- Completed and used (after review): validators, account service, controller, routes, `ForgotPasswordActivity`.
- Failed: first Android attempt routed to a weak 8B model and produced unusable code (discarded). Several large requests timed out
  upstream (OVH/Kilo free tiers), then rate limits (~80 s cooldowns) hit.
- Written by Claude instead: 3 layouts + drawable, `AccountSecurityActivity`, `AuthFlow`, string files, Retrofit models, wiring.
- Review fixes to generated code: `ForgotPasswordActivity` (info text never shown; password was trimmed), `accountService`
  (cleaner timestamp ordering, explicit `id::text` cast, same-email check case-insensitive).

## Tests
- `node --check` on all backend files: **pass**.
- New SQL (sessions list, revoke, email change + unique violation, account deletion, re-registration with the freed
  email/username) run against a real **PostgreSQL 16** with `migrations/001_core_schema.sql` applied: **pass**.
- XML well-formedness and every `R.id` / `R.string` / `R.layout` reference cross-checked: **pass**.
- **Not run:** Node server with dependencies (installs were blocked here), Android compile/APK build (no SDK here),
  any on-device test. Please run the checks below on your side.

## Manual acceptance checklist (run on your phone)
1. `npm install && npm start` (or redeploy on Render). `GET /health` returns `db: up`.
2. App: register, log in immediately (no verification), log out.
3. Forgot password: request code, read it from Render logs (or email if SMTP set), reset, log in with the new password.
4. Account security: change password (forces re-login); change email; second device/emulator login shows 2 sessions, log one out; log out of all devices.
5. Deactivate, then log in again (reactivates). Delete account, confirm the same email can register again.

## Known issues / decisions needed (not changed by me)
1. `android/android/` is a byte-identical duplicate of `android/app` etc. It is ignored by Gradle; delete it when convenient.
2. `levia-debug.apk` is committed to the repo; it is stale and not included in the Phase 2 zip. Add `*.apk` to `.gitignore`.
3. `src/config/db.js` uses `ssl: { rejectUnauthorized: false }` (no certificate check on the database connection). Recommended: pin the Supabase CA before launch.
4. Tokens are in plain `SharedPreferences` (already flagged in Phase 1); Keystore hardening is still planned.
5. Account deletion hides posts (`deleted_at`) but does not yet purge comments/likes or media; revisit in Phases 4-5.

## Next: Phase 3 (profiles and follows)
Profile read/edit, public/private toggle, follow / unfollow, follow requests, followers/following lists.
