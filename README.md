# Oasis

A personal video game tracker for Android. Log what you play, calendar-style
(Letterboxd for games), with monthly/yearly summaries and separate libraries
per platform. Game info and cover art come from Wikipedia and archive.org.

Dark charcoal UI with glowing neon-blue borders throughout.

## Features

- **Main menu** — Monthly Tracker, Yearly Tracker, and a tile per platform
  (PC, PS5, PS4, Xbox Series X|S, Xbox One, Switch, Switch 2, Retro/Other).
- **Per-platform library** — search Wikipedia/archive.org for a game and add
  it to that platform's shelf.
- **Game diary** — a month calendar per game; tap a day to log hours played
  (and optional notes). Highlighted days show hours at a glance.
- **Monthly/Yearly Tracker** — total hours and a per-game breakdown for the
  selected month or year (yearly also breaks totals down by month).
- **In-app updates** — no Play Store. The app checks this repo's latest
  GitHub Release on launch and offers to download + install the new APK
  in place, keeping your logged data.
- **Steam** — connect your Steam account (OpenID sign-in, no password shared
  with the app) to browse your library and per-game achievement completion.

## How updates work

Every push to `main` runs `.github/workflows/release.yml`, which:

1. Builds a signed release APK with `versionCode` = the CI run number.
2. Publishes it as a GitHub Release tagged `vNN`.

The installed app checks `GET /repos/ALF452/Project-Oasis/releases/latest`
on launch. If the release's version number is higher than the running
build, it shows an **UPDATE AVAILABLE** banner, downloads the APK via
`DownloadManager`, and hands it to the system installer through a
`FileProvider` URI. Because every build is signed with the same key, Android
treats it as an upgrade — your Room database (games, log entries) is kept,
no uninstall needed.

The first time you install an update this way, Android will ask you to
allow Oasis to install unknown apps — that's a one-time per-app permission,
not a full "unknown sources" toggle.

### One-time setup: signing secrets

Because this repository is public, the signing keystore is **not** committed
to it — it lives only in GitHub Actions secrets. Before the release workflow
can produce an installable APK, add these four repository secrets under
**Settings → Secrets and variables → Actions**:

- `KEYSTORE_BASE64` — base64 of the `.jks` keystore file
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

These were generated once and handed to you directly (not stored in this
repo). If you ever lose them, you'd need to generate a new keystore — but
note that any future APK would then no longer be treated as an "update" by
devices that already have the old-key version installed; they'd need a full
reinstall once.

### Optional: Steam integration

Steam achievements need one more secret, `STEAM_API_KEY` — a free key from
https://steamcommunity.com/dev/apikey (any domain name works when
registering it, e.g. "localhost"). Without this secret the build still
succeeds; the Steam screen just shows it isn't configured yet.

Unlike the signing keystore, this key isn't tied to end users — it's the
app's own key for reading *public* Steam profile/game data on behalf of
whoever connects their account, the same way sites like SteamDB or
PSNProfiles-style trackers work. Keep it out of the repo regardless, since
it's rate-limited and tied to a personal Steam account.

## Project structure

- `app/src/main/java/com/oasis/tracker/data` — Room entities/DAOs and the
  platform list.
- `app/src/main/java/com/oasis/tracker/network` — Wikipedia + archive.org
  search, and Steam OpenID login + Web API (`network/steam`).
- `app/src/main/java/com/oasis/tracker/update` — GitHub release check, APK
  download/install flow.
- `app/src/main/java/com/oasis/tracker/ui` — Compose screens, navigation,
  and the neon/charcoal theme.

## Building locally

Requires JDK 17 and the Android SDK (compileSdk 35). This sandbox couldn't
reach `dl.google.com` to run a full Gradle build, so the first CI run on
`main` is the first real compile — check the Actions tab after pushing and
report back anything that fails.

```
./gradlew assembleDebug   # unsigned debug build, installable as-is
./gradlew assembleRelease # needs the signing env vars/secrets above
```
