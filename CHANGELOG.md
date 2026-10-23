# Changelog

All notable changes to Edit Pro Studio (Android). Newest first.

## 2.1.0 — unreleased

A reliability and usability pass driven by the incremental improvement roadmap
(Section 1 in full, plus the highest-value items from Sections 2 and 3).

### Fixed
- **Editor shortcuts no longer steal keystrokes.** The workspace handled keys with
  `onPreviewKeyEvent`, which runs before the focused child, so typing in the project-name
  or media-search field triggered editor actions instead: `s` split the clip, space toggled
  playback, `Delete` removed clips, `t` added a text clip. Both fields now report focus and
  the global handler stands down while either has it.
- **Saving can no longer crash the app.** The Save / Open / subtitle-import / preset-export
  callbacks had no error handling; a failed write threw out of the activity callback. They
  now report success only after the write completes and surface failures in a dialog.
- **Rendered videos are no longer left in the cache.** Exports were written to
  `cacheDir/exports`, which Android may clear at any time. They now go to the app's private
  files directory, and the folder is capped at the newest 12 renders.
- **The exported video is reachable at last.** `lastExportedFilePath` was set but never read,
  and the declared `FileProvider` was never used, so a finished render was unreachable. The
  export dialog now shows the file (name + size) with **Open** and **Share** actions.
- **The global Reset no longer deletes keyframes.** It silently wiped animation; the Motion
  tab's *Clear keyframes* is now the explicit way to do that.

### Added
- **Project home screen** shown before the editor: New / Open / Continue, plus a recent-project
  list recorded on save. It states plainly that Android apps cannot browse storage on their
  own, and *Remove* only clears the list entry — never the saved file.
- **Confirm before discarding work**: New / Open / Load-demo ask first when the timeline has
  content.
- **Cancel a render**, including a whole render queue. The busy flag is always cleared so the
  UI cannot get stuck.
- **Import reporting**: a batch import now says how many files were imported, skipped as
  duplicates, or could not be read, and names them.
- **Duplicate detection** on import (same name + byte size + duration); the redundant copy is
  deleted so no orphan files accumulate.
- **Missing-media state**: assets with no local copy are flagged in the library, and the health
  scan reports media files that have disappeared from the device.
- **Group-scoped resets**: *Reset transform* (Layout tab) and *Reset colour & effects* (Effects
  tab) touch only their own group.
- Touch targets now meet a 40dp minimum and action buttons declare `Role.Button`.

### Changed
- Render-queue results now report successes and failures instead of a bare "finished" message.
- Five controls that had no effect on the rendered output are now labelled **"· not rendered
  yet"** in the inspector (Blur, Hue Shift, Film Grain, Shutter Motion Blur, Chromatic
  Aberration). They still store their values for future use — they are just no longer
  presented as if they did something.

## 2.0.0
- Initial Android port: multi-track timeline, colour grading with 3-way wheels and scopes,
  motion presets and 3D rigs, audio EQ/mastering, and native `MediaCodec` + `MediaMuxer`
  H.264 export.
