# LEVIA Android + backend source

## Android project

The Android application source is in `android/`. It uses Java 8, Android Gradle Plugin 4.0.2, Gradle 6.1.1, Retrofit 2.9.0, OkHttp 3.14.9 and the existing API base URL:
`https://levia-19ac.onrender.com/`

The duplicate `android/android/` project from the supplied archive was removed from this working copy. The root `android/app/` source was retained because it contains the later Phase 2 account-security and Phase 3 API integration.

### Build on Termux

The supplied ZIP did not contain `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`. Its committed Phase 3 APK was an older build and has been omitted so it is not mistaken for this revision.

Use Java 8 and Gradle 6.1.1. One way to build without an existing wrapper is:

```sh
pkg install wget unzip
# Make sure the active Java runtime is Java 8.
wget https://services.gradle.org/distributions/gradle-6.1.1-bin.zip
unzip gradle-6.1.1-bin.zip
cd LEVIA/android
../gradle-6.1.1/bin/gradle assembleDebug
```

To generate the wrapper and then use your requested command:

```sh
../gradle-6.1.1/bin/gradle wrapper --gradle-version 6.1.1
chmod +x gradlew
./gradlew assembleDebug
```

Install the resulting APK:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Install Android SDK platform 30 and build tools 29.0.3. Do not change the AGP/Gradle versions without checking compatibility.


## Implemented in this revision

- Premium dark-violet visual system, aurora backgrounds, rounded glass-like panels and lavender accents.
- Animated splash logo and saved-session validation using the existing `/api/v1/me` endpoint.
- Redesigned login and registration screens; original Retrofit calls, token storage and validation remain in place.
- Existing forgot-password and account-security flows retained and restyled.
- Main navigation shell with Home, Search, Create and Profile.
- Home empty state explicitly explains that the feed is awaiting backend feed APIs; it does not fabricate posts or stories.
- User search uses `GET /api/v1/users/search?q=&limit=`.
- Follow action uses `POST /api/v1/users/:userId/follow` and displays the server-returned `following` or `requested` state.
- Profile screen uses `GET /api/v1/profile/me`, displays real profile counts, and edits display name/bio through `PATCH /api/v1/profile/me`.
- Followers and following counts open lists using the existing `/api/v1/users/:username/followers` and `/following` endpoints.
- Existing account-security capabilities remain in the source: password/email changes, session listing/revocation, logout-all, deactivate and delete.

## Backend work still required for the complete social product

The supplied backend currently has authentication, account security, profiles, user search, follow relationships and follow requests. It does not expose app-facing APIs for:

- Feed and post pagination
- Create/edit/delete posts and media upload
- Stories
- Likes, comments, shares and saves
- Notifications list/read state
- User profile post grid and media retrieval
- Profile picture upload/storage (the profile PATCH accepts an HTTPS `avatarUrl`, but no upload endpoint is supplied)
- Public profile detail screen and in-app follow-request inbox

The Android UI intentionally avoids pretending these unsupported operations succeeded.

## Verification limits

XML files were parsed and Java `R.id`, `R.string`, `R.layout`, `R.drawable`, `R.color` and `R.style` references were cross-checked against resources. A real Android Gradle build and device run could not be performed in this environment: the supplied archive omitted the Gradle wrapper launcher/JAR, and no Android SDK/Gradle installation is available here. Therefore this package is **not represented as a verified APK build**.
