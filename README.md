# Edit Pro Studio — Native Android Video Editor

A native **Android / Kotlin / Jetpack Compose** video-editing studio. It is a 100% on-device
app — no Node, no React Native, no web layer and no third-party editing SDK. Video is rendered
and exported straight through Android's own `MediaCodec` + `MediaMuxer` hardware pipeline.

This repository is set up to be **built into an APK by GitHub Actions** the moment you push it.

---

## What's inside

| Area | Details |
| --- | --- |
| UI | Jetpack Compose + Material 3, custom dark/light palette, macOS-style traffic-light header |
| Workspace modes | **Normal** (edit), **Colour** (grading + scopes), **Motion** (3D rigs/keyframes), **Music** (EQ/mastering) |
| Timeline | 6 tracks (Video 1, Video 2, Overlay, Text & Titles, Audio 1, Audio 2), magnetic snapping, ripple delete, split, keyframes |
| Colour | 58 look presets, 3-way colour wheels, live RGB parade / waveform / histogram scopes, split grade wipe |
| Motion | 55+ motion presets, cubic-bezier curve editor, 3D camera rigs (tilt/swivel/anamorphic) |
| Audio | 5-band EQ, mastering presets, volume automation, beat detection, LUFS meter |
| Export | Native H.264 MP4 via `MediaCodec`, resolution/aspect/CRF/fps controls, render queue, range in/out |
| Storage | Media imported through the Storage Access Framework; projects saved as portable `.filmcraft` JSON |

Package id: `com.editpro.studio` · minSdk 26 · targetSdk/compileSdk 34

See [CHANGELOG.md](CHANGELOG.md) for what changed in each version, and
[docs/MAINTENANCE.md](docs/MAINTENANCE.md) for how the project is verified, a manual test
checklist, which effects actually reach the rendered output, and the known gaps.

---

## Build it locally

### Requirements
- **JDK 17** (Temurin recommended)
- **Android SDK** with platform 34 + build-tools (Android Studio installs these for you)

### From the command line
```bash
# 1. Point Gradle at your SDK (or just open the project in Android Studio)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # macOS: ~/Library/Android/sdk

# 2. Build the installable debug APK
./gradlew assembleDebug

# 3. (optional) run the unit tests
./gradlew testDebugUnitTest
```
The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

Or simply open the project folder in **Android Studio** and press **Run ▶**.

---

## Build it with GitHub Actions (no local setup)

Push this repository to GitHub. The workflow in
[`.github/workflows/android-build.yml`](.github/workflows/android-build.yml) runs on every push
to `main`/`master` and on every pull request, and it also has a manual **Run workflow** button.

It will:
1. Set up JDK 17 + the Android SDK.
2. Run the JVM unit tests.
3. Build the **debug** and **release** APKs.
4. Upload them as downloadable workflow artifacts (`EditPro-debug-apk`, `EditPro-release-unsigned-apk`).

**To download the APK:** open the **Actions** tab → pick the latest run → scroll to **Artifacts** →
download `EditPro-debug-apk`. Unzip it, copy `app-debug.apk` to your phone and install it
(enable "Install unknown apps" for your file manager first).

### Cutting a tagged release
Push a version tag and the APKs are attached to a GitHub Release automatically:
```bash
git tag v2.0.0
git push origin v2.0.0
```

> The **debug** APK is signed with the auto-generated debug key, so it installs on any device.
> The **release** APK is produced *unsigned* because no signing secrets are configured — see below.

---

## Signing a release build (optional)

1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 \
     -validity 10000 -alias editpro
   ```
2. Add these repository secrets (**Settings → Secrets and variables → Actions**):
   `SIGNING_KEY` (base64 of `release.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
3. Add a `signingConfigs` block to `app/build.gradle.kts` reading those values and point the
   `release` build type at it.

Until then, use the debug APK for testing and sideloading.

---

## Project layout

```
EditProAndroid/
├── .github/workflows/android-build.yml   # CI: builds APKs + tests
├── build.gradle.kts                      # root build script (plugin versions)
├── settings.gradle.kts                   # module + repository config
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/                       # Gradle 8.9 wrapper
└── app/
    ├── build.gradle.kts                  # app module, dependencies, lint
    ├── proguard-rules.pro                # R8 rules for the release build
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/editpro/studio/
        │   │   ├── MainActivity.kt               # Compose host + workspace layout
        │   │   ├── viewmodel/EditProViewModel.kt  # single source of truth (UiState)
        │   │   ├── model/                         # data classes, timeline engine, presets, serialization
        │   │   ├── media/                         # MediaEngine + NativeVideoExporter (MediaCodec)
        │   │   └── ui/                            # Compose panels, theme, icons, dialogs
        │   └── res/                               # strings, theme, icons, file paths
        └── test/java/com/editpro/studio/          # JVM unit tests (timeline engine, serialization)
```

## Tech stack
Kotlin 1.9.24 · AGP 8.5.2 · Gradle 8.9 · Jetpack Compose (BOM 2024.06.00) · Material 3 ·
Coroutines 1.8.1 · kotlinx-serialization 1.6.3 · AndroidX Lifecycle/Activity.
