# Maintenance notes

Practical notes for whoever works on this app next. Everything here was written by reading
the source; where something has not actually been run, it says so.

## How this project is verified

**Compilation is verified automatically; behaviour is not.** Every push runs the GitHub Actions
workflow, which executes the JVM unit tests and builds both APKs. A green run means the code
compiles and the existing unit tests pass — it does **not** mean any UI flow was exercised.
There is no instrumentation test suite.

The unit tests cover the pure logic only: the timeline engine (snapping, collision resolution,
cubic-bezier interpolation, audio envelope, SRT parsing), preset catalog sizes, project
serialization round-trip, and export dimension maths.

Anything touching Compose UI, MediaCodec, the file system or the Storage Access Framework has
**never been run in an automated test**. Treat those changes as unverified until someone
installs the APK and tries them.

## Manual test checklist

Worth walking through after any change to the editor. Roughly ordered by how much it would hurt
if it broke.

1. **Save / open round-trip** — add clips, Save to a `.filmcraft` file, New, then Open it back.
   Confirm the timeline, markers and clip properties survive.
2. **Save failure path** — save to a location you cannot write (or cancel the picker) and confirm
   you get a "SAVE FAILED" dialog rather than a crash.
3. **Shortcut/typing separation** — click into the project-name field and type a name containing
   `s`, `t`, spaces and Delete. Nothing on the timeline should change.
4. **Export and retrieve** — render a short project (2–5 s), then use **Open** and **Share** in
   the export dialog. Confirm the file plays, has sound, and shows up in your Photos/Gallery app
   under `Movies/EditPro`.
5. **Cancel a render** — start a longer render and hit *Cancel export*. The progress bar should
   stop, the button should return to *Render MP4*, and the app should stay usable.
6. **Import batch** — import several files at once, including one unsupported file and one
   duplicate. Confirm the summary names what was skipped.
7. **Home screen** — relaunch the app, confirm the home screen appears, that *Continue* returns
   to the editor, and that *Remove* on a recent entry does not delete the file.
8. **Confirm dialogs** — with clips on the timeline, press New, Open and Demo. Each should ask
   first; Cancel should leave everything untouched.
9. **Undo/redo** — after each of the above, undo and redo and confirm the timeline returns to
   the expected states.
10. **Rendered output vs preview** — see the table below.
11. **Audio in preview** — import an audio file, add it to an audio track, press play and confirm
    you hear it; then mute that track and confirm it goes silent.
12. **Right-click a clip** — with a mouse, secondary-click a clip and confirm the context menu
    opens (long-press still works for touch).
13. **Advertised shortcuts** — try Ctrl+S, Ctrl+O, Ctrl+E, Ctrl+N, Ctrl+I, Ctrl+K and Ctrl+Alt+P.

## What actually renders

The preview (`PreviewStageUi`) and the exporter (`NativeVideoExporter`) both composite the
timeline, but through different pipelines: the preview uses Compose drawing, the export uses an
Android `Canvas` plus `ColorMatrixColorFilter`. Some controls only reach one of them, and a few
reach neither.

| Control | Preview | Export |
| --- | --- | --- |
| Brightness, Contrast, Saturation | yes | yes |
| Temperature, Tint | yes | yes |
| Lift / Gamma / Gain (3-way wheels) | yes | yes |
| Sepia, Invert | yes | yes |
| Vignette | yes | yes |
| Sharpen | small brightness boost only | same |
| Scale, Opacity, Rotation, Position, Flip | yes | yes |
| 3D Tilt / Swivel | yes | yes |
| Anamorphic Squeeze | yes | yes |
| Text / titles | yes | yes |
| **Blur** | **no** | **no** |
| **Hue Shift** | **no** | **no** |
| **Film Grain** | **no** | **no** |
| **Shutter Motion Blur** | **no** | **no** |
| **Chromatic Aberration** | **no** | **no** |

The five controls in bold now carry a **"· not rendered yet"** label in the inspector so they are
not mistaken for working effects. Their values are still stored in the project file, so
implementing them later will light up existing projects rather than requiring a migration.

Audio is a partial story. **Volume, fades and volume automation are applied** in both preview and
export; the export mixes every audible clip into one stereo AAC track at 44.1 kHz. **Pan, EQ,
compression, de-essing and reverb are stored and shown in the UI but are not applied** to either
the preview or the exported audio.

## Known gaps and follow-ups

- **Export audio is a basic mix.** Volume, fades and automation are honoured; pan, EQ,
  compression, de-essing and reverb are not. Audio is capped at 5 minutes per render, resampling
  is nearest-neighbour, and clip `speed` shifts pitch rather than time-stretching. Solo is not
  re-resolved for export (mute and hide are).
- **Gallery publishing is best-effort.** On Android 10+ it goes through MediaStore and needs no
  permission; on Android 8–9 it needs `WRITE_EXTERNAL_STORAGE` and falls back to a message if
  that is denied. A publish failure never fails the export.
- **Exports accumulate** in the app's files directory. The folder is capped at the newest 12
  renders; if a longer retention policy is wanted, that cap is in `NativeVideoExporter`.
- **No instrumentation tests.** The manual checklist above is the substitute.
- **`file_paths.xml` still declares a `projects/` files path** that nothing writes to. Harmless,
  but it can be removed.
- **Preset organisation** (roadmap Section 2, item 6) is partly present: presets are grouped in
  the preset hub and there is JSON import/export with validation. Per-category search inside the
  inspector's look list exists; a global preset search does not.
- **Face tracking** (roadmap Section 3, item 4) was deliberately not attempted. It needs a real
  tracking model, and adding one would mean either a large on-device dependency or a network
  service, which the project's rules rule out. There is no placeholder button for it.

## Release signing

Release builds are signed only when a keystore is configured, and stay unsigned otherwise — the
build never fails because of it. To enable signing, add four repository secrets
(`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`); the workflow decodes them
into `KEYSTORE_FILE` and the build picks them up. All builds must use the same keystore, or an
update will not install over a previously installed copy.
