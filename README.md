# LEVIA

LEVIA is an Android community app backed by an Express + PostgreSQL API.

## Project layout

- `android/` — Android Java application (this is the only Android project directory).
- `src/` — Express API backend.
- `migrations/` — PostgreSQL schema. Apply `001_core_schema.sql` to the configured database before deploying.

## Android build

The Android app uses Java 8 source compatibility, Android Gradle Plugin 8.7.3, Gradle 8.9, and Android SDK 35.

From the `android/` directory:

```bash
~/gradle-8.9/bin/gradle assembleDebug
```

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.

## Backend

Install dependencies and start the API:

```bash
npm install
npm start
```

Configure the environment using `.env.example`. Never commit production secrets.

### Social endpoints added for the Android app

- `GET /api/v1/posts/feed`
- `GET /api/v1/posts/search?q=...`
- `GET /api/v1/users/:username/posts`
- `POST /api/v1/posts`
- `POST /api/v1/posts/:postId/like`
- `DELETE /api/v1/posts/:postId/like`
- `GET /api/v1/posts/:postId/comments`
- `POST /api/v1/posts/:postId/comments`

These endpoints use the existing `posts`, `likes`, and `comments` tables from migration `001_core_schema.sql` and require a valid bearer access token. Existing authentication, profile, follow, and account-security routes are retained.
