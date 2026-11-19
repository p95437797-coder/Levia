# LEVIA - Phase 3: Profiles and follows (backend)

Status: **backend code complete, NOT run.** Only `node --check` was possible here (no PostgreSQL, no npm install).
No database migration is needed: every table used already exists in `migrations/001_core_schema.sql`.

## How it was built
Code was drafted by FreeLLMAPI (model `gpt-oss-20b` via OVH, 2 requests, both completed). Claude saved the files and
fixed the defects listed below. Unchanged generated files: both validators, both controllers, `followRoutes.js`.

## New / changed files
| File | Status |
| --- | --- |
| `src/validators/profileValidators.js` | new (FreeLLMAPI, unchanged) |
| `src/controllers/profileController.js` | new (FreeLLMAPI, unchanged) |
| `src/services/profileService.js` | new (FreeLLMAPI draft, 5 fixes) |
| `src/routes/profileRoutes.js` | new (Claude, small) |
| `src/validators/followValidators.js` | new (FreeLLMAPI, unchanged) |
| `src/controllers/followController.js` | new (FreeLLMAPI, unchanged) |
| `src/services/followService.js` | new (FreeLLMAPI draft, 6 fixes) |
| `src/routes/followRoutes.js` | new (FreeLLMAPI, unchanged) |
| `src/middleware/auth.js` | changed: added `optionalAuthenticate` |
| `src/routes/index.js` | changed: mounts profile + follow routes |

## Endpoints (base `/api/v1`)
| Method | Path | Auth | Notes |
| --- | --- | --- | --- |
| GET | `/profile/me` | required | own profile, counts, settings |
| PATCH | `/profile/me` | required | `displayName, bio, avatarUrl (https), isPrivate`; going public auto-accepts pending requests |
| GET / PATCH | `/profile/settings` | required | `showOnlineStatus, likedPostsPrivate, allowMessagesFrom, language` |
| GET | `/users/search?q=&limit=` | optional | blocked users hidden; prefix matches first |
| GET | `/users/:username` | optional | 404 if missing/inactive/blocked either way; `canViewPosts`, `relationship` |
| POST / DELETE | `/users/:userId/follow` | required | public -> `following`; private -> `requested`; DELETE also cancels a pending request |
| GET | `/follow-requests?limit=&cursor=` | required | incoming pending requests |
| POST | `/follow-requests/:requesterId` | required | body `{ action: "accept" | "reject" }` |
| DELETE | `/followers/:followerId` | required | remove one of your followers |
| GET | `/users/:username/followers` and `/following` | optional | private account: 403 unless viewer follows it (or is the owner); cursor = ISO timestamp |

## Defects found in the generated drafts (fixed)
profileService: (1) `searchUsers` used `const` then `+=` (crash on every search with a logged-in viewer); (2) `updateSettings`
built a broken parameter list; (3) `getMyProfile` read settings in parallel with the insert that creates them, and hard-coded
different defaults than the schema; (4) search ranking compared against the same pattern as the filter; (5) added: going public
did not resolve pending requests.
followService: (1) re-requesting a follow on a private account that already had a pending/accepted request caused a unique
violation (HTTP 500); (2) **private followers/following lists were readable by anonymous users**; (3) block relations were
not hidden in lists; (4) accept/reject checked then updated outside one guarded statement (race); (5) follow notifications
had noise columns / dead ternaries; (6) request list ignored blocked and inactive requesters.

## Not done in Phase 3
- **Android screens** (profile view/edit, follow button, requests list) are not included. Backend only.
- No automated tests. Suggested first checks (curl with two accounts): follow public user -> `following`; set one private,
  follow -> `requested`, accept -> follower list shows it; anonymous GET `/users/<private>/followers` -> 403.

## Tests
- `node --check` on every file in `src/`: **pass**.
- **Not run:** server with dependencies, any SQL against PostgreSQL, any request. Run `npm install && npm start` and the checks above.
