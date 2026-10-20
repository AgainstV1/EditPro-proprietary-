package com.editpro.studio

import com.editpro.studio.model.AssetKind
import com.editpro.studio.model.ClipFilters
import com.editpro.studio.model.ExportSettings
import com.editpro.studio.model.Keyframe
import com.editpro.studio.model.MediaAsset
import com.editpro.studio.model.PresetCatalog
import com.editpro.studio.model.ProjectSerializer
import com.editpro.studio.model.ProjectSettings
import com.editpro.studio.model.TimelineClip
import com.editpro.studio.model.TimelineEngine
import com.editpro.studio.model.TimelineMarker
import com.editpro.studio.model.TimelineTrack
import com.editpro.studio.model.VolumePoint
import com.editpro.studio.model.formatTimecode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditProEngineTest {

    @Test
    fun testFormatTimecode() {
        assertEquals("00:00.00", formatTimecode(0.0))
        assertEquals("01:05.50", formatTimecode(65.5))
        assertEquals("10:00.00", formatTimecode(600.0))
    }

    @Test
    fun testMagneticSnappingAndCollisionResolution() {
        val asset = MediaAsset(id = "a1", name = "Clip1.mp4", kind = AssetKind.VIDEO, durationSec = 10.0)
        val assetsById = mapOf("a1" to asset)
        val clipA = TimelineClip(id = "c1", assetId = "a1", track = TimelineTrack.VIDEO, startAt = 0.0, trimIn = 0.0, trimOut = 5.0)
        val clipB = TimelineClip(id = "c2", assetId = "a1", track = TimelineTrack.VIDEO, startAt = 8.0, trimIn = 0.0, trimOut = 4.0)
        val all = listOf(clipA, clipB)

        // 1. Magnetic snap to end of clipA (5.0s) when within 11px at scale 75px/s (5.08s -> 6px distance)
        val snapped = TimelineEngine.resolveTimelineStart(
            desired = 5.08,
            duration = 3.0,
            track = TimelineTrack.VIDEO,
            clipId = "c2",
            scalePxPerSec = 75.0,
            direction = -1.0,
            allClips = all,
            assetsById = assetsById,
            playhead = 0.0,
            magneticSnap = true
        )
        assertEquals(5.0, snapped, 1e-4)

        // 2. Non-overlapping collision resolution when dragged onto clipA (desired = 2.0s, moving right)
        val pushedRight = TimelineEngine.resolveTimelineStart(
            desired = 2.0,
            duration = 4.0,
            track = TimelineTrack.VIDEO,
            clipId = "c2",
            scalePxPerSec = 75.0,
            direction = 1.0,
            allClips = all,
            assetsById = assetsById,
            playhead = 0.0,
            magneticSnap = false
        )
        assertEquals(5.0, pushedRight, 1e-4)
    }

    @Test
    fun testCubicBezierAndKeyframeInterpolation() {
        val linearMid = TimelineEngine.evalCubicBezier(0.5, 0.0, 0.0, 1.0, 1.0)
        assertEquals(0.5, linearMid, 1e-3)

        val smoothMid = TimelineEngine.evalCubicBezier(0.5, 0.42, 0.0, 0.58, 1.0)
        assertEquals(0.5, smoothMid, 1e-2)

        val clip = TimelineClip(
            id = "kclip",
            scale = 100.0,
            opacity = 100.0,
            ease = "linear",
            keyframes = listOf(
                Keyframe(time = 0.0, scale = 100.0, opacity = 0.0, x = -10.0, y = 0.0),
                Keyframe(time = 2.0, scale = 200.0, opacity = 100.0, x = 10.0, y = 20.0)
            )
        )
        val mid = TimelineEngine.animatedProps(clip, 1.0)
        assertEquals(150.0, mid.scale, 1e-3)
        assertEquals(50.0, mid.opacity, 1e-3)
        assertEquals(0.0, mid.x, 1e-3)
        assertEquals(10.0, mid.y, 1e-3)
    }

    @Test
    fun testAudioEnvelopeAndAutomation() {
        val clip = TimelineClip(
            id = "aud1",
            track = TimelineTrack.AUDIO,
            fadeInAudio = 1.0,
            fadeOutAudio = 1.0,
            volumeKeyframes = listOf(
                VolumePoint(time = 0.0, value = 100.0),
                VolumePoint(time = 2.0, value = 150.0),
                VolumePoint(time = 4.0, value = 100.0)
            )
        )
        // Halfway through 1.0s fade-in -> fade multiplier = 0.5, automation at 0.5s = 1.125 -> 0.5625
        val gFadeIn = TimelineEngine.audioEnvelope(clip, 0.5, 4.0)
        assertEquals(0.5 * 1.125, gFadeIn, 1e-3)

        // At peak 2.0s -> no fade attenuation, automation = 1.5
        val gPeak = TimelineEngine.audioEnvelope(clip, 2.0, 4.0)
        assertEquals(1.5, gPeak, 1e-3)
    }

    @Test
    fun testSrtParser() {
        val srt = """
            1
            00:00:01,500 --> 00:00:04,000
            <b>Hello EditPro Studio</b>
            Second line

            2
            00:00:05,000 --> 00:00:07,250
            Subtitle Two
        """.trimIndent()

        val clips = TimelineEngine.parseSrtToClips(srt)
        assertEquals(2, clips.size)
        assertEquals(1.5, clips[0].startAt, 1e-3)
        assertEquals(2.5, clips[0].trimOut, 1e-3)
        assertEquals("Hello EditPro Studio\nSecond line", clips[0].text)
        assertEquals(5.0, clips[1].startAt, 1e-3)
        assertEquals(2.25, clips[1].trimOut, 1e-3)
    }

    @Test
    fun testPresetCatalogAndSerializationRoundtrip() {
        assertTrue(PresetCatalog.lookPresets.size >= 58)
        assertTrue(PresetCatalog.allMotionNames.size >= 55)
        assertTrue(PresetCatalog.builtinEquipmentRigs.size >= 14)
        assertTrue(PresetCatalog.builtinAudioGear.size >= 12)

        val sampleClip = PresetCatalog.applyMotionToClip(
            TimelineClip(id = "mc1", scale = 100.0),
            "Ken Burns",
            4.0
        )
        assertEquals(2, sampleClip.keyframes.size)

        val json = ProjectSerializer.serializeProject(
            name = "Unit Test Project",
            settings = ProjectSettings(1920, 1080, 30),
            clips = listOf(sampleClip),
            markers = listOf(TimelineMarker(1.25, "Beat 1")),
            assets = emptyList()
        )
        val restored = ProjectSerializer.deserializeProject(json)
        assertEquals("Unit Test Project", restored.name)
        assertEquals(1920, restored.settings.width)
        assertEquals(1, restored.clips.size)
        assertEquals(2, restored.clips.first().keyframes.size)
        assertEquals(1, restored.markers.size)
    }

    @Test
    fun testExportDimensionsAndLowRamGuard() {
        val portraitSettings = ExportSettings(aspect = "portrait", resolution = "1920x1080", androidLowRamGuard = false)
        assertEquals(1080 to 1920, portraitSettings.resolvedDimensions(deviceRamGb = 8))

        val lowRamSettings = ExportSettings(aspect = "landscape", resolution = "1920x1080", androidLowRamGuard = true)
        assertEquals(1280 to 720, lowRamSettings.resolvedDimensions(deviceRamGb = 4))
    }
}
