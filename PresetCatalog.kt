package com.editpro.studio.model

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class LookPreset(
    val id: String,
    val title: String,
    val category: String,
    val filters: ClipFilters
)

data class MotionKeyframeStep(
    val timeSec: Double,
    val scale: Double? = null,
    val opacity: Double? = null,
    val rotation: Double? = null,
    val x: Double? = null,
    val y: Double? = null
)

data class EquipmentRigPreset(
    val name: String,
    val category: String,
    val scale: Double = 100.0,
    val filters: ClipFilters? = null,
    val rig: CameraRig = CameraRig(),
    val motionPresetName: String? = null
)

data class AudioGearPreset(
    val name: String,
    val eq: List<Double>, // 5 bands: 60Hz, 250Hz, 1kHz, 4kHz, 12kHz
    val comp: Double,
    val pan: Double,
    val deess: Double,
    val reverb: Double,
    val volume: Double
)

object PresetCatalog {

    // -------------------------------------------------------------------------
    // 1. ALL 58 BUILT-IN COLOUR & LOOK PRESETS FROM EDITPRO
    // -------------------------------------------------------------------------
    val lookPresets: List<LookPreset> = listOf(
        LookPreset("cinematic", "Cinematic", "cinema", ClipFilters(brightness = 96.0, contrast = 118.0, saturation = 84.0, temperature = 8.0, sharpen = 18.0, vignette = 22.0)),
        LookPreset("vivid", "Vivid", "color", ClipFilters(brightness = 105.0, contrast = 112.0, saturation = 145.0, sharpen = 25.0)),
        LookPreset("mono", "Black & white", "mono", ClipFilters(contrast = 112.0, saturation = 0.0, sharpen = 12.0)),
        LookPreset("dreamy", "Dreamy", "social", ClipFilters(brightness = 108.0, contrast = 92.0, saturation = 112.0, blur = 1.0, temperature = 8.0, tint = 4.0, vignette = 12.0)),
        LookPreset("vintage", "Vintage", "film", ClipFilters(brightness = 96.0, contrast = 94.0, saturation = 75.0, temperature = 16.0, tint = 8.0, vignette = 28.0)),
        LookPreset("tealorange", "Teal & Orange", "cinema", ClipFilters(contrast = 120.0, saturation = 125.0, temperature = 14.0, tint = -10.0, vignette = 18.0, sharpen = 15.0)),
        LookPreset("noir", "Noir", "mono", ClipFilters(brightness = 96.0, contrast = 145.0, saturation = 0.0, vignette = 38.0, grain = 35.0)),
        LookPreset("film", "Film", "film", ClipFilters(contrast = 108.0, saturation = 88.0, temperature = 7.0, grain = 30.0, vignette = 20.0)),
        LookPreset("warm", "Warm", "color", ClipFilters(brightness = 103.0, saturation = 110.0, temperature = 32.0)),
        LookPreset("cool", "Cool", "color", ClipFilters(saturation = 96.0, temperature = -32.0, tint = 5.0)),
        LookPreset("matte", "Matte", "film", ClipFilters(brightness = 108.0, contrast = 86.0, saturation = 84.0)),
        LookPreset("sepia", "Sepia", "film", ClipFilters(contrast = 106.0, sepia = 85.0, vignette = 20.0)),
        LookPreset("punch", "Punch", "color", ClipFilters(contrast = 130.0, saturation = 132.0, sharpen = 40.0)),
        LookPreset("goldenhour", "Golden Hour", "cinema", ClipFilters(brightness = 106.0, contrast = 108.0, saturation = 118.0, temperature = 42.0, tint = 5.0, vignette = 12.0)),
        LookPreset("bluehour", "Blue Hour", "cinema", ClipFilters(brightness = 90.0, contrast = 112.0, saturation = 90.0, temperature = -45.0, tint = 10.0, vignette = 18.0)),
        LookPreset("blockbuster", "Blockbuster", "cinema", ClipFilters(contrast = 128.0, saturation = 115.0, temperature = 10.0, tint = -12.0, vignette = 26.0, sharpen = 22.0)),
        LookPreset("bleachbypass", "Bleach Bypass", "cinema", ClipFilters(contrast = 145.0, saturation = 52.0, temperature = -4.0, grain = 12.0, vignette = 20.0)),
        LookPreset("softfilm", "Soft Film", "film", ClipFilters(brightness = 106.0, contrast = 90.0, saturation = 92.0, temperature = 10.0, blur = 0.3, grain = 10.0)),
        LookPreset("super8", "Super 8", "film", ClipFilters(brightness = 98.0, contrast = 110.0, saturation = 88.0, temperature = 24.0, grain = 45.0, vignette = 32.0)),
        LookPreset("fadedfilm", "Faded Film", "film", ClipFilters(brightness = 112.0, contrast = 78.0, saturation = 72.0, temperature = 8.0, grain = 18.0)),
        LookPreset("polaroid", "Polaroid", "film", ClipFilters(brightness = 108.0, contrast = 92.0, saturation = 88.0, temperature = 18.0, tint = 4.0, vignette = 12.0)),
        LookPreset("kodakwarm", "Kodak Warm", "film", ClipFilters(brightness = 103.0, contrast = 105.0, saturation = 112.0, temperature = 26.0, tint = 2.0, grain = 8.0)),
        LookPreset("fuji", "Fuji Green", "film", ClipFilters(contrast = 105.0, saturation = 95.0, temperature = -5.0, tint = -12.0, sharpen = 12.0)),
        LookPreset("portra", "Portra", "film", ClipFilters(brightness = 104.0, contrast = 96.0, saturation = 92.0, temperature = 14.0, tint = 3.0, sharpen = 8.0)),
        LookPreset("chrome", "Chrome", "film", ClipFilters(contrast = 125.0, saturation = 120.0, temperature = -5.0, sharpen = 25.0, vignette = 8.0)),
        LookPreset("pastel", "Pastel", "color", ClipFilters(brightness = 112.0, contrast = 82.0, saturation = 82.0, temperature = 8.0, tint = 8.0)),
        LookPreset("neon", "Neon Pop", "creative", ClipFilters(contrast = 135.0, saturation = 175.0, temperature = -8.0, tint = 18.0, sharpen = 22.0)),
        LookPreset("cyberpunk", "Cyberpunk", "creative", ClipFilters(brightness = 92.0, contrast = 140.0, saturation = 155.0, temperature = -28.0, tint = 28.0, vignette = 30.0, grain = 12.0)),
        LookPreset("purplehaze", "Purple Haze", "creative", ClipFilters(brightness = 104.0, contrast = 100.0, saturation = 118.0, temperature = -8.0, tint = 38.0, blur = 0.2)),
        LookPreset("pinkglow", "Pink Glow", "creative", ClipFilters(brightness = 108.0, contrast = 94.0, saturation = 130.0, temperature = 18.0, tint = 25.0, blur = 0.4)),
        LookPreset("arctic", "Arctic", "color", ClipFilters(brightness = 105.0, contrast = 112.0, saturation = 86.0, temperature = -40.0, tint = -5.0, sharpen = 18.0)),
        LookPreset("desert", "Desert Sand", "color", ClipFilters(brightness = 104.0, contrast = 105.0, saturation = 105.0, temperature = 34.0, tint = 12.0, vignette = 10.0)),
        LookPreset("forest", "Forest Green", "color", ClipFilters(contrast = 112.0, saturation = 105.0, temperature = -8.0, tint = -24.0, sharpen = 15.0)),
        LookPreset("ocean", "Deep Ocean", "color", ClipFilters(brightness = 94.0, contrast = 118.0, saturation = 118.0, temperature = -28.0, tint = -8.0, vignette = 15.0)),
        LookPreset("sunset", "Sunset", "social", ClipFilters(brightness = 102.0, contrast = 112.0, saturation = 130.0, temperature = 45.0, tint = 12.0, vignette = 12.0)),
        LookPreset("moody", "Moody", "social", ClipFilters(brightness = 82.0, contrast = 125.0, saturation = 78.0, temperature = -8.0, vignette = 34.0, grain = 12.0)),
        LookPreset("romantic", "Romantic", "social", ClipFilters(brightness = 106.0, contrast = 92.0, saturation = 108.0, temperature = 18.0, tint = 8.0, blur = 0.5, vignette = 15.0)),
        LookPreset("clean", "Clean Natural", "social", ClipFilters(brightness = 102.0, contrast = 102.0, saturation = 100.0, sharpen = 12.0)),
        LookPreset("brightday", "Bright Day", "social", ClipFilters(brightness = 118.0, contrast = 96.0, saturation = 110.0, temperature = 6.0, sharpen = 8.0)),
        LookPreset("nightlife", "Nightlife", "social", ClipFilters(brightness = 85.0, contrast = 132.0, saturation = 135.0, temperature = -18.0, tint = 15.0, vignette = 20.0)),
        LookPreset("documentary", "Documentary", "cinema", ClipFilters(brightness = 98.0, contrast = 108.0, saturation = 88.0, temperature = 2.0, sharpen = 16.0)),
        LookPreset("sports", "Sports Pop", "social", ClipFilters(brightness = 105.0, contrast = 128.0, saturation = 140.0, sharpen = 35.0)),
        LookPreset("food", "Food Warmth", "social", ClipFilters(brightness = 108.0, contrast = 108.0, saturation = 120.0, temperature = 22.0, sharpen = 12.0)),
        LookPreset("skinsoft", "Soft Portrait", "social", ClipFilters(brightness = 105.0, contrast = 92.0, saturation = 96.0, temperature = 8.0, blur = 0.35)),
        LookPreset("highkey", "High Key", "creative", ClipFilters(brightness = 125.0, contrast = 85.0, saturation = 90.0, temperature = 6.0)),
        LookPreset("lowkey", "Low Key", "creative", ClipFilters(brightness = 76.0, contrast = 128.0, saturation = 82.0, vignette = 32.0)),
        LookPreset("hardcontrast", "Hard Contrast", "creative", ClipFilters(contrast = 155.0, saturation = 108.0, sharpen = 28.0)),
        LookPreset("crossprocess", "Cross Process", "creative", ClipFilters(contrast = 125.0, saturation = 120.0, temperature = 22.0, tint = -18.0, grain = 8.0)),
        LookPreset("negative", "Negative", "creative", ClipFilters(contrast = 115.0, saturation = 115.0, invert = 100.0)),
        LookPreset("grainy", "Grainy Film", "film", ClipFilters(contrast = 110.0, saturation = 86.0, temperature = 8.0, grain = 58.0, vignette = 20.0)),
        LookPreset("mist", "Morning Mist", "social", ClipFilters(brightness = 114.0, contrast = 80.0, saturation = 82.0, blur = 0.8, temperature = 6.0)),
        LookPreset("dramatic", "Dramatic", "cinema", ClipFilters(brightness = 88.0, contrast = 142.0, saturation = 92.0, sharpen = 22.0, vignette = 35.0)),
        LookPreset("retro80", "Retro 80s", "creative", ClipFilters(contrast = 115.0, saturation = 138.0, temperature = 12.0, tint = 18.0, grain = 18.0)),
        LookPreset("retro90", "Retro 90s", "creative", ClipFilters(contrast = 108.0, saturation = 105.0, temperature = 8.0, tint = -8.0, grain = 14.0)),
        LookPreset("teal", "Teal Cool", "color", ClipFilters(saturation = 105.0, temperature = -20.0, tint = -18.0)),
        LookPreset("amber", "Amber Glow", "color", ClipFilters(saturation = 112.0, temperature = 48.0, tint = 6.0)),
        LookPreset("crisp", "Crisp Detail", "color", ClipFilters(contrast = 112.0, saturation = 105.0, sharpen = 55.0)),
        LookPreset("softfocus", "Soft Focus", "creative", ClipFilters(brightness = 105.0, contrast = 90.0, saturation = 105.0, blur = 1.3, vignette = 8.0)),
        LookPreset("reset", "Reset effects", "all", ClipFilters())
    )

    // -------------------------------------------------------------------------
    // 2. ALL 56+ MOTION PRESETS & COLLECTIONS FROM EDITPRO
    // -------------------------------------------------------------------------
    val motionCollections: Map<String, List<String>> = linkedMapOf(
        "Cinematic Camera Moves" to listOf(
            "Zoom in", "Zoom out", "Pan left", "Pan right", "Ken Burns",
            "Slow Push In", "Slow Pull Out", "Cinematic Drift",
            "Parallax Drift Left", "Parallax Drift Right", "IMAX Slow Hero Push",
            "Anamorphic Lens Breathe", "Gimbal Orbit Left", "Gimbal Orbit Right",
            "FPV Drone Dive", "Crane Jib Rise", "Crane Jib Descend",
            "Dolly Zoom Vertigo", "Luxury Slow Pan & Scale"
        ),
        "Smooth Reveals & Titles" to listOf(
            "Fade in/out", "Fade Up", "Fade Down", "Slide in left", "Slide in right",
            "Reveal From Bottom", "Reveal From Top", "Focus Settle",
            "Ease In Zoom", "Ease Out Zoom", "Kinetic Title Slam"
        ),
        "Punchy Social & Impact" to listOf(
            "Pop in", "Drop in", "Shake", "Drop Bounce", "Bounce In",
            "Overshoot Zoom", "Zoom Snap", "Flash Pop", "Elastic Pop",
            "Heartbeat", "Pulse", "Pulse Slow", "Social Reel Hook Pop", "Glitch RGB Twitch"
        ),
        "Whips, Spins & Transitions" to listOf(
            "Zoom & Rotate", "Whip Pan Left", "Whip Pan Right",
            "Anamorphic Whip Transition", "Spin in", "Spin Out", "Spin Reveal",
            "Swing In", "Slide Out Left", "Slide Out Right", "Slide Out Up",
            "Slide Out Down", "Dutch Angle Tension", "Dutch Snap Release", "Speed Ramp Whip Zoom"
        ),
        "Handheld & Atmospheric" to listOf(
            "Handheld Drift", "Micro Jitter", "Float", "Diagonal Drift",
            "Vertical Pan Up", "Vertical Pan Down", "Side-to-Side Rock",
            "Tilt Clockwise", "Tilt Counterclockwise", "3D Floating Product Showcase",
            "Documentary Shoulder Rig", "Pendulum Swing"
        )
    )

    val allMotionNames: List<String> = motionCollections.values.flatten().distinct()

    fun generateMotionSteps(name: String, d: Double): List<MotionKeyframeStep> {
        val dur = max(0.1, d)
        return when (name) {
            "Zoom in" -> listOf(MotionKeyframeStep(0.0, scale = 100.0), MotionKeyframeStep(dur, scale = 120.0))
            "Zoom out" -> listOf(MotionKeyframeStep(0.0, scale = 125.0), MotionKeyframeStep(dur, scale = 100.0))
            "Pan left" -> listOf(MotionKeyframeStep(0.0, scale = 115.0, x = 6.0), MotionKeyframeStep(dur, scale = 115.0, x = -6.0))
            "Pan right" -> listOf(MotionKeyframeStep(0.0, scale = 115.0, x = -6.0), MotionKeyframeStep(dur, scale = 115.0, x = 6.0))
            "Ken Burns" -> listOf(MotionKeyframeStep(0.0, scale = 100.0, x = -3.0, y = 2.0), MotionKeyframeStep(dur, scale = 125.0, x = 4.0, y = -3.0))
            "Slide in left" -> listOf(MotionKeyframeStep(0.0, x = -100.0), MotionKeyframeStep(min(0.6, dur / 2.0), x = 0.0))
            "Slide in right" -> listOf(MotionKeyframeStep(0.0, x = 100.0), MotionKeyframeStep(min(0.6, dur / 2.0), x = 0.0))
            "Drop in" -> listOf(MotionKeyframeStep(0.0, y = -100.0, opacity = 0.0), MotionKeyframeStep(min(0.6, dur / 2.0), y = 0.0, opacity = 100.0))
            "Pop in" -> {
                val a = min(0.5, dur / 2.0)
                listOf(
                    MotionKeyframeStep(0.0, scale = 50.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.7, scale = 108.0, opacity = 100.0),
                    MotionKeyframeStep(a, scale = 100.0)
                )
            }
            "Spin in" -> listOf(
                MotionKeyframeStep(0.0, rotation = -180.0, scale = 30.0, opacity = 0.0),
                MotionKeyframeStep(min(0.8, dur / 2.0), rotation = 0.0, scale = 100.0, opacity = 100.0)
            )
            "Fade in/out" -> listOf(
                MotionKeyframeStep(0.0, opacity = 0.0),
                MotionKeyframeStep(min(0.6, dur / 3.0), opacity = 100.0),
                MotionKeyframeStep(max(dur - 0.6, dur * 2.0 / 3.0), opacity = 100.0),
                MotionKeyframeStep(dur, opacity = 0.0)
            )
            "Pulse" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                var t = 0.0
                var n = 0
                while (t <= min(dur, 12.0)) {
                    out.add(MotionKeyframeStep(t, scale = if (n % 2 != 0) 100.0 else 106.0))
                    t += 0.5
                    n++
                }
                out
            }
            "Shake" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                var t = 0.0
                var n = 0
                while (t <= min(dur, 1.2)) {
                    out.add(MotionKeyframeStep(t, x = if (n % 2 != 0) 1.2 else -1.2, y = if (n % 3 != 0) 0.6 else -0.6))
                    t += 0.08
                    n++
                }
                out
            }
            "Slow Push In" -> listOf(MotionKeyframeStep(0.0, scale = 100.0), MotionKeyframeStep(dur, scale = 116.0))
            "Slow Pull Out" -> listOf(MotionKeyframeStep(0.0, scale = 118.0), MotionKeyframeStep(dur, scale = 100.0))
            "Vertical Pan Up" -> listOf(MotionKeyframeStep(0.0, scale = 112.0, y = 7.0), MotionKeyframeStep(dur, scale = 112.0, y = -7.0))
            "Vertical Pan Down" -> listOf(MotionKeyframeStep(0.0, scale = 112.0, y = -7.0), MotionKeyframeStep(dur, scale = 112.0, y = 7.0))
            "Diagonal Drift" -> listOf(MotionKeyframeStep(0.0, scale = 110.0, x = -5.0, y = 4.0), MotionKeyframeStep(dur, scale = 118.0, x = 5.0, y = -4.0))
            "Reveal From Bottom" -> listOf(
                MotionKeyframeStep(0.0, y = 35.0, opacity = 0.0),
                MotionKeyframeStep(min(0.7, dur * 0.35), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = 0.0, opacity = 100.0)
            )
            "Reveal From Top" -> listOf(
                MotionKeyframeStep(0.0, y = -35.0, opacity = 0.0),
                MotionKeyframeStep(min(0.7, dur * 0.35), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = 0.0, opacity = 100.0)
            )
            "Bounce In" -> {
                val a = min(0.7, dur * 0.28)
                listOf(
                    MotionKeyframeStep(0.0, scale = 65.0, opacity = 0.0, y = 10.0),
                    MotionKeyframeStep(a * 0.65, scale = 112.0, opacity = 100.0, y = -2.0),
                    MotionKeyframeStep(a, scale = 96.0, y = 1.0),
                    MotionKeyframeStep(min(a * 1.35, dur), scale = 100.0, y = 0.0),
                    MotionKeyframeStep(dur, scale = 100.0, opacity = 100.0, y = 0.0)
                )
            }
            "Overshoot Zoom" -> {
                val a = min(0.6, dur * 0.3)
                listOf(
                    MotionKeyframeStep(0.0, scale = 75.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.7, scale = 124.0, opacity = 100.0),
                    MotionKeyframeStep(a, scale = 96.0),
                    MotionKeyframeStep(min(a * 1.4, dur), scale = 100.0),
                    MotionKeyframeStep(dur, scale = 100.0, opacity = 100.0)
                )
            }
            "Tilt Clockwise" -> listOf(MotionKeyframeStep(0.0, rotation = -5.0, scale = 108.0), MotionKeyframeStep(dur, rotation = 5.0, scale = 108.0))
            "Tilt Counterclockwise" -> listOf(MotionKeyframeStep(0.0, rotation = 5.0, scale = 108.0), MotionKeyframeStep(dur, rotation = -5.0, scale = 108.0))
            "Spin Out" -> listOf(
                MotionKeyframeStep(0.0, rotation = 0.0, scale = 100.0, opacity = 100.0),
                MotionKeyframeStep(dur, rotation = 180.0, scale = 45.0, opacity = 0.0)
            )
            "Spin Reveal" -> {
                val a = min(0.8, dur * 0.35)
                listOf(
                    MotionKeyframeStep(0.0, rotation = 90.0, scale = 60.0, opacity = 0.0),
                    MotionKeyframeStep(a, rotation = 0.0, scale = 100.0, opacity = 100.0),
                    MotionKeyframeStep(dur, rotation = 0.0, scale = 100.0, opacity = 100.0)
                )
            }
            "Swing In" -> {
                val a = min(0.7, dur * 0.3)
                listOf(
                    MotionKeyframeStep(0.0, rotation = -22.0, x = -12.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.7, rotation = 8.0, x = 3.0, opacity = 100.0),
                    MotionKeyframeStep(a, rotation = -3.0, x = -1.0),
                    MotionKeyframeStep(min(a * 1.35, dur), rotation = 0.0, x = 0.0),
                    MotionKeyframeStep(dur, rotation = 0.0, x = 0.0, opacity = 100.0)
                )
            }
            "Elastic Pop" -> {
                val a = min(0.8, dur * 0.32)
                listOf(
                    MotionKeyframeStep(0.0, scale = 20.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.45, scale = 130.0, opacity = 100.0),
                    MotionKeyframeStep(a * 0.68, scale = 88.0),
                    MotionKeyframeStep(a * 0.84, scale = 106.0),
                    MotionKeyframeStep(a, scale = 100.0),
                    MotionKeyframeStep(dur, scale = 100.0, opacity = 100.0)
                )
            }
            "Pulse Slow" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                val step = max(0.2, dur / 8.0)
                var t = 0.0
                while (t < dur) {
                    val odd = ((t / step).roundToInt() % 2) != 0
                    out.add(MotionKeyframeStep(t, scale = if (odd) 100.0 else 104.0))
                    t += step
                }
                out.add(MotionKeyframeStep(dur, scale = 100.0))
                out
            }
            "Heartbeat" -> {
                val a = min(0.28, dur / 4.0)
                listOf(
                    MotionKeyframeStep(0.0, scale = 100.0),
                    MotionKeyframeStep(a * 0.35, scale = 112.0),
                    MotionKeyframeStep(a * 0.7, scale = 100.0),
                    MotionKeyframeStep(a, scale = 108.0),
                    MotionKeyframeStep(a * 1.5, scale = 100.0),
                    MotionKeyframeStep(dur, scale = 100.0)
                )
            }
            "Flash Pop" -> {
                val a = min(0.25, dur / 3.0)
                listOf(
                    MotionKeyframeStep(0.0, opacity = 0.0, scale = 115.0),
                    MotionKeyframeStep(a * 0.5, opacity = 100.0, scale = 100.0),
                    MotionKeyframeStep(a, opacity = 100.0, scale = 100.0),
                    MotionKeyframeStep(dur, opacity = 100.0, scale = 100.0)
                )
            }
            "Fade Up" -> listOf(
                MotionKeyframeStep(0.0, y = 14.0, opacity = 0.0),
                MotionKeyframeStep(min(0.6, dur * 0.35), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = 0.0, opacity = 100.0)
            )
            "Fade Down" -> listOf(
                MotionKeyframeStep(0.0, y = -14.0, opacity = 0.0),
                MotionKeyframeStep(min(0.6, dur * 0.35), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = 0.0, opacity = 100.0)
            )
            "Whip Pan Left" -> {
                val a = min(0.28, dur * 0.2)
                listOf(
                    MotionKeyframeStep(0.0, x = 28.0, opacity = 30.0, rotation = 2.0, scale = 110.0),
                    MotionKeyframeStep(a, x = 0.0, opacity = 100.0, rotation = 0.0, scale = 100.0),
                    MotionKeyframeStep(dur, x = 0.0, opacity = 100.0, rotation = 0.0, scale = 100.0)
                )
            }
            "Whip Pan Right" -> {
                val a = min(0.28, dur * 0.2)
                listOf(
                    MotionKeyframeStep(0.0, x = -28.0, opacity = 30.0, rotation = -2.0, scale = 110.0),
                    MotionKeyframeStep(a, x = 0.0, opacity = 100.0, rotation = 0.0, scale = 100.0),
                    MotionKeyframeStep(dur, x = 0.0, opacity = 100.0, rotation = 0.0, scale = 100.0)
                )
            }
            "Handheld Drift" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                val step = max(0.08, min(0.18, dur / 18.0))
                var t = 0.0
                var n = 0
                while (t < dur) {
                    val sign = if (n % 2 != 0) 1.0 else -1.0
                    out.add(MotionKeyframeStep(t, scale = 108.0, x = sign * (1.2 + (n % 3) * 0.35), y = if (n % 3 != 0) -0.8 else 1.0))
                    t += step
                    n++
                }
                out.add(MotionKeyframeStep(dur, scale = 108.0, x = 0.0, y = 0.0))
                out
            }
            "Micro Jitter" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                val step = max(0.045, min(0.09, dur / 24.0))
                var t = 0.0
                var n = 0
                while (t < min(dur, 1.6)) {
                    out.add(
                        MotionKeyframeStep(
                            t,
                            x = if (n % 2 != 0) 1.8 else -1.8,
                            y = if (n % 3 != 0) 1.0 else -1.0,
                            rotation = if (n % 2 != 0) 0.5 else -0.5
                        )
                    )
                    t += step
                    n++
                }
                out.add(MotionKeyframeStep(dur, x = 0.0, y = 0.0, rotation = 0.0))
                out
            }
            "Float" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                val step = max(0.25, min(0.6, dur / 8.0))
                var t = 0.0
                while (t < dur) {
                    out.add(MotionKeyframeStep(t, y = sin(t * 2.2) * 2.5, scale = 102.0))
                    t += step
                }
                out.add(MotionKeyframeStep(dur, y = 0.0, scale = 102.0))
                out
            }
            "Parallax Drift Left" -> listOf(MotionKeyframeStep(0.0, scale = 120.0, x = 8.0, y = 1.0), MotionKeyframeStep(dur, scale = 120.0, x = -8.0, y = -1.0))
            "Parallax Drift Right" -> listOf(MotionKeyframeStep(0.0, scale = 120.0, x = -8.0, y = -1.0), MotionKeyframeStep(dur, scale = 120.0, x = 8.0, y = 1.0))
            "Zoom Snap" -> {
                val a = min(0.22, dur * 0.22)
                listOf(
                    MotionKeyframeStep(0.0, scale = 100.0),
                    MotionKeyframeStep(a * 0.65, scale = 132.0),
                    MotionKeyframeStep(a, scale = 112.0),
                    MotionKeyframeStep(min(a * 1.5, dur), scale = 100.0),
                    MotionKeyframeStep(dur, scale = 100.0)
                )
            }
            "Zoom & Rotate" -> listOf(MotionKeyframeStep(0.0, scale = 100.0, rotation = -3.0, x = -2.0), MotionKeyframeStep(dur, scale = 118.0, rotation = 3.0, x = 2.0))
            "Cinematic Drift" -> listOf(MotionKeyframeStep(0.0, scale = 108.0, x = -2.0, y = 1.0), MotionKeyframeStep(dur, scale = 114.0, x = 2.0, y = -1.0))
            "Side-to-Side Rock" -> listOf(
                MotionKeyframeStep(0.0, rotation = -2.0, scale = 105.0),
                MotionKeyframeStep(dur * 0.25, rotation = 2.0, scale = 105.0),
                MotionKeyframeStep(dur * 0.5, rotation = -2.0, scale = 105.0),
                MotionKeyframeStep(dur * 0.75, rotation = 2.0, scale = 105.0),
                MotionKeyframeStep(dur, rotation = 0.0, scale = 105.0)
            )
            "Drop Bounce" -> {
                val a = min(0.8, dur * 0.3)
                listOf(
                    MotionKeyframeStep(0.0, y = -35.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.55, y = 5.0, opacity = 100.0),
                    MotionKeyframeStep(a * 0.75, y = -3.0),
                    MotionKeyframeStep(a, y = 0.0),
                    MotionKeyframeStep(dur, y = 0.0, opacity = 100.0)
                )
            }
            "Slide Out Left" -> listOf(
                MotionKeyframeStep(0.0, x = 0.0, opacity = 100.0),
                MotionKeyframeStep(max(0.0, dur - min(0.6, dur * 0.3)), x = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, x = -100.0, opacity = 0.0)
            )
            "Slide Out Right" -> listOf(
                MotionKeyframeStep(0.0, x = 0.0, opacity = 100.0),
                MotionKeyframeStep(max(0.0, dur - min(0.6, dur * 0.3)), x = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, x = 100.0, opacity = 0.0)
            )
            "Slide Out Up" -> listOf(
                MotionKeyframeStep(0.0, y = 0.0, opacity = 100.0),
                MotionKeyframeStep(max(0.0, dur - min(0.6, dur * 0.3)), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = -100.0, opacity = 0.0)
            )
            "Slide Out Down" -> listOf(
                MotionKeyframeStep(0.0, y = 0.0, opacity = 100.0),
                MotionKeyframeStep(max(0.0, dur - min(0.6, dur * 0.3)), y = 0.0, opacity = 100.0),
                MotionKeyframeStep(dur, y = 100.0, opacity = 0.0)
            )
            "Ease In Zoom" -> listOf(MotionKeyframeStep(0.0, scale = 100.0), MotionKeyframeStep(dur, scale = 135.0))
            "Ease Out Zoom" -> listOf(MotionKeyframeStep(0.0, scale = 135.0), MotionKeyframeStep(dur, scale = 100.0))
            "Focus Settle" -> {
                val a = min(0.55, dur * 0.3)
                listOf(
                    MotionKeyframeStep(0.0, scale = 112.0, opacity = 70.0),
                    MotionKeyframeStep(a, scale = 100.0, opacity = 100.0),
                    MotionKeyframeStep(dur, scale = 100.0, opacity = 100.0)
                )
            }
            "IMAX Slow Hero Push" -> listOf(
                MotionKeyframeStep(0.0, scale = 100.0, opacity = 100.0, rotation = 0.0, x = 0.0, y = 0.0),
                MotionKeyframeStep(dur, scale = 124.0, opacity = 100.0, rotation = 0.0, x = 0.0, y = -2.0)
            )
            "Anamorphic Whip Transition" -> listOf(
                MotionKeyframeStep(0.0, scale = 118.0, opacity = 20.0, rotation = -3.0, x = 32.0, y = 0.0),
                MotionKeyframeStep(dur * 0.25, scale = 100.0, opacity = 100.0, rotation = 0.0, x = 0.0, y = 0.0),
                MotionKeyframeStep(dur, scale = 104.0, opacity = 100.0, rotation = 0.0, x = 0.0, y = 0.0)
            )
            "3D Floating Product Showcase" -> listOf(
                MotionKeyframeStep(0.0, scale = 106.0, opacity = 100.0, rotation = -2.0, x = -3.0, y = 2.0),
                MotionKeyframeStep(dur * 0.5, scale = 114.0, opacity = 100.0, rotation = 2.0, x = 3.0, y = -2.0),
                MotionKeyframeStep(dur, scale = 106.0, opacity = 100.0, rotation = -2.0, x = -3.0, y = 2.0)
            )
            "Anamorphic Lens Breathe" -> listOf(
                MotionKeyframeStep(0.0, scale = 104.0),
                MotionKeyframeStep(dur * 0.5, scale = 112.0),
                MotionKeyframeStep(dur, scale = 104.0)
            )
            "Gimbal Orbit Left" -> listOf(
                MotionKeyframeStep(0.0, scale = 116.0, x = 9.0, y = -2.0, rotation = -1.5),
                MotionKeyframeStep(dur, scale = 116.0, x = -9.0, y = 2.0, rotation = 1.5)
            )
            "Gimbal Orbit Right" -> listOf(
                MotionKeyframeStep(0.0, scale = 116.0, x = -9.0, y = 2.0, rotation = 1.5),
                MotionKeyframeStep(dur, scale = 116.0, x = 9.0, y = -2.0, rotation = -1.5)
            )
            "FPV Drone Dive" -> listOf(
                MotionKeyframeStep(0.0, scale = 100.0, y = -12.0, rotation = -6.0),
                MotionKeyframeStep(dur * 0.65, scale = 142.0, y = 4.0, rotation = 4.0),
                MotionKeyframeStep(dur, scale = 160.0, y = 0.0, rotation = 0.0)
            )
            "Crane Jib Rise" -> listOf(MotionKeyframeStep(0.0, scale = 114.0, y = 12.0), MotionKeyframeStep(dur, scale = 102.0, y = -8.0))
            "Crane Jib Descend" -> listOf(MotionKeyframeStep(0.0, scale = 102.0, y = -8.0), MotionKeyframeStep(dur, scale = 114.0, y = 12.0))
            "Dolly Zoom Vertigo" -> listOf(
                MotionKeyframeStep(0.0, scale = 100.0, y = 0.0),
                MotionKeyframeStep(dur * 0.5, scale = 134.0, y = -3.0),
                MotionKeyframeStep(dur, scale = 108.0, y = 2.0)
            )
            "Dutch Angle Tension" -> listOf(MotionKeyframeStep(0.0, scale = 110.0, rotation = 0.0), MotionKeyframeStep(dur, scale = 122.0, rotation = -8.0))
            "Dutch Snap Release" -> {
                val a = min(0.45, dur * 0.3)
                listOf(
                    MotionKeyframeStep(0.0, scale = 118.0, rotation = 9.0),
                    MotionKeyframeStep(a, scale = 100.0, rotation = 0.0),
                    MotionKeyframeStep(dur, scale = 100.0, rotation = 0.0)
                )
            }
            "Speed Ramp Whip Zoom" -> {
                val m = dur * 0.5
                listOf(
                    MotionKeyframeStep(0.0, scale = 100.0),
                    MotionKeyframeStep(m * 0.7, scale = 104.0),
                    MotionKeyframeStep(m, scale = 145.0),
                    MotionKeyframeStep(m * 1.3, scale = 112.0),
                    MotionKeyframeStep(dur, scale = 115.0)
                )
            }
            "Documentary Shoulder Rig" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                val step = max(0.12, dur / 14.0)
                var t = 0.0
                var n = 0
                while (t <= dur) {
                    out.add(MotionKeyframeStep(t, scale = 106.0, x = sin(n * 1.7) * 1.4, y = cos(n * 1.3) * 1.1, rotation = sin(n.toDouble()) * 0.4))
                    t += step
                    n++
                }
                out
            }
            "Kinetic Title Slam" -> {
                val a = min(0.38, dur * 0.25)
                listOf(
                    MotionKeyframeStep(0.0, scale = 220.0, opacity = 0.0, y = -15.0),
                    MotionKeyframeStep(a * 0.7, scale = 94.0, opacity = 100.0, y = 2.0),
                    MotionKeyframeStep(a, scale = 100.0, opacity = 100.0, y = 0.0),
                    MotionKeyframeStep(dur, scale = 100.0, opacity = 100.0, y = 0.0)
                )
            }
            "Glitch RGB Twitch" -> {
                val out = mutableListOf<MotionKeyframeStep>()
                var t = 0.0
                var n = 0
                while (t <= min(dur, 1.5)) {
                    out.add(MotionKeyframeStep(t, scale = if (n % 3 == 0) 108.0 else 100.0, x = if (n % 2 != 0) 3.2 else -3.2, y = if (n % 4 != 0) 1.2 else -1.2))
                    t += 0.07
                    n++
                }
                out.add(MotionKeyframeStep(dur, scale = 100.0, x = 0.0, y = 0.0))
                out
            }
            "Luxury Slow Pan & Scale" -> listOf(MotionKeyframeStep(0.0, scale = 108.0, x = -4.0, y = 1.0), MotionKeyframeStep(dur, scale = 118.0, x = 4.0, y = -1.0))
            "Social Reel Hook Pop" -> {
                val a = min(0.32, dur * 0.22)
                listOf(
                    MotionKeyframeStep(0.0, scale = 78.0, y = 18.0, opacity = 0.0),
                    MotionKeyframeStep(a * 0.65, scale = 110.0, y = -3.0, opacity = 100.0),
                    MotionKeyframeStep(a, scale = 100.0, y = 0.0, opacity = 100.0),
                    MotionKeyframeStep(dur, scale = 104.0, y = 0.0, opacity = 100.0)
                )
            }
            "Pendulum Swing" -> listOf(
                MotionKeyframeStep(0.0, rotation = -6.0, scale = 108.0),
                MotionKeyframeStep(dur * 0.33, rotation = 6.0, scale = 108.0),
                MotionKeyframeStep(dur * 0.66, rotation = -4.0, scale = 108.0),
                MotionKeyframeStep(dur, rotation = 0.0, scale = 108.0)
            )
            else -> listOf(MotionKeyframeStep(0.0, scale = 100.0), MotionKeyframeStep(dur, scale = 115.0))
        }
    }

    fun applyMotionToClip(clip: TimelineClip, presetName: String, durationSec: Double): TimelineClip {
        val steps = generateMotionSteps(presetName, durationSec)
        val baseScaleFactor = clip.scale / 100.0
        val generatedKeyframes = steps.map { step ->
            Keyframe(
                time = (step.timeSec * 1000.0).roundToInt() / 1000.0,
                scale = (step.scale ?: 100.0) * baseScaleFactor,
                opacity = step.opacity ?: clip.opacity,
                rotation = step.rotation ?: clip.rotation,
                x = clip.x + (step.x ?: 0.0),
                y = clip.y + (step.y ?: 0.0),
                filters = clip.filters
            )
        }
        return clip.copy(keyframes = generatedKeyframes)
    }

    // -------------------------------------------------------------------------
    // 3. BUILT-IN CAMERA & LENS EQUIPMENT RIGS
    // -------------------------------------------------------------------------
    val builtinEquipmentRigs: Map<String, EquipmentRigPreset> = linkedMapOf(
        "ARRI Alexa Mini LF · Cooke S4 35mm" to EquipmentRigPreset(
            name = "ARRI Alexa Mini LF · Cooke S4 35mm",
            category = "cinema",
            scale = 102.0,
            filters = ClipFilters(brightness = 99.0, contrast = 112.0, saturation = 94.0, temperature = 6.0, tint = -2.0, vignette = 16.0, grain = 10.0, sharpen = 12.0, liftR = 0.0, liftG = 1.0, liftB = 3.0, gammaR = 2.0, gammaG = 1.0, gammaB = -2.0, gainR = 4.0, gainG = 2.0, gainB = -3.0),
            rig = CameraRig(tiltX = 0.0, swivelY = 0.0, shutter = 180.0, aberration = 1.0, anamorphic = 0.0)
        ),
        "RED V-Raptor 8K · Atlas Orion 2x Anamorphic" to EquipmentRigPreset(
            name = "RED V-Raptor 8K · Atlas Orion 2x Anamorphic",
            category = "cinema",
            scale = 108.0,
            filters = ClipFilters(brightness = 97.0, contrast = 124.0, saturation = 108.0, temperature = -4.0, tint = 4.0, vignette = 24.0, grain = 8.0, sharpen = 24.0, liftR = -2.0, liftG = 2.0, liftB = 8.0, gainR = 8.0, gainG = 3.0, gainB = -5.0),
            rig = CameraRig(tiltX = 0.0, swivelY = 0.0, shutter = 180.0, aberration = 3.0, anamorphic = 18.0)
        ),
        "Sony Venice 2 · Zeiss Supreme Prime" to EquipmentRigPreset(
            name = "Sony Venice 2 · Zeiss Supreme Prime",
            category = "cinema",
            scale = 100.0,
            filters = ClipFilters(brightness = 101.0, contrast = 114.0, saturation = 102.0, temperature = 2.0, tint = -3.0, vignette = 10.0, grain = 5.0, sharpen = 20.0),
            rig = CameraRig()
        ),
        "16mm Bolex H16 · Kern Switar Vintage" to EquipmentRigPreset(
            name = "16mm Bolex H16 · Kern Switar Vintage",
            category = "vintage",
            scale = 104.0,
            filters = ClipFilters(brightness = 96.0, contrast = 118.0, saturation = 82.0, temperature = 18.0, tint = 6.0, vignette = 34.0, grain = 46.0, sharpen = 6.0, sepia = 14.0),
            rig = CameraRig(shutter = 170.0, aberration = 4.0)
        ),
        "Super 8 Kodak Ektachrome 100D" to EquipmentRigPreset(
            name = "Super 8 Kodak Ektachrome 100D",
            category = "vintage",
            scale = 106.0,
            filters = ClipFilters(brightness = 98.0, contrast = 122.0, saturation = 116.0, temperature = 26.0, tint = 10.0, vignette = 38.0, grain = 54.0, blur = 0.4),
            rig = CameraRig(shutter = 150.0, aberration = 6.0)
        ),
        "IMAX 70mm 15-Perf Grand Format" to EquipmentRigPreset(
            name = "IMAX 70mm 15-Perf Grand Format",
            category = "cinema",
            scale = 105.0,
            filters = ClipFilters(brightness = 102.0, contrast = 126.0, saturation = 112.0, temperature = 4.0, vignette = 12.0, grain = 6.0, sharpen = 36.0),
            rig = CameraRig()
        ),
        "Panavision C-Series Anamorphic 40mm" to EquipmentRigPreset(
            name = "Panavision C-Series Anamorphic 40mm",
            category = "cinema",
            scale = 110.0,
            filters = ClipFilters(brightness = 98.0, contrast = 116.0, saturation = 96.0, temperature = 10.0, tint = -6.0, vignette = 22.0, grain = 14.0, sharpen = 14.0),
            rig = CameraRig(aberration = 2.0, anamorphic = 22.0)
        ),
        "iPhone 16 Pro Apple Log · Black Pro-Mist 1/4" to EquipmentRigPreset(
            name = "iPhone 16 Pro Apple Log · Black Pro-Mist 1/4",
            category = "social",
            scale = 100.0,
            filters = ClipFilters(brightness = 104.0, contrast = 106.0, saturation = 104.0, temperature = 8.0, tint = 2.0, blur = 0.25, vignette = 10.0, sharpen = 10.0),
            rig = CameraRig(aberration = 1.0)
        ),
        "DJI Ronin 4D · 4-Axis Gimbal Float" to EquipmentRigPreset(
            name = "DJI Ronin 4D · 4-Axis Gimbal Float",
            category = "rig",
            scale = 108.0,
            filters = ClipFilters(brightness = 100.0, contrast = 108.0, saturation = 102.0, sharpen = 15.0),
            rig = CameraRig(),
            motionPresetName = "Cinematic Drift"
        ),
        "Leica Summilux-C · Dreamy Portrait Glass" to EquipmentRigPreset(
            name = "Leica Summilux-C · Dreamy Portrait Glass",
            category = "cinema",
            scale = 103.0,
            filters = ClipFilters(brightness = 104.0, contrast = 96.0, saturation = 98.0, temperature = 12.0, tint = 4.0, vignette = 18.0, blur = 0.2),
            rig = CameraRig(aberration = 1.0)
        ),
        "3D Perspective Hero Tilt Left" to EquipmentRigPreset(
            name = "3D Perspective Hero Tilt Left",
            category = "rig",
            scale = 112.0,
            rig = CameraRig(tiltX = 8.0, swivelY = -16.0, shutter = 220.0, aberration = 2.0)
        ),
        "3D Perspective Hero Tilt Right" to EquipmentRigPreset(
            name = "3D Perspective Hero Tilt Right",
            category = "rig",
            scale = 112.0,
            rig = CameraRig(tiltX = 8.0, swivelY = 16.0, shutter = 220.0, aberration = 2.0)
        ),
        "Top-Down Isometric Table Rig" to EquipmentRigPreset(
            name = "Top-Down Isometric Table Rig",
            category = "rig",
            scale = 115.0,
            rig = CameraRig(tiltX = 24.0, swivelY = -8.0, shutter = 180.0)
        ),
        "Split-Diopter De Palma Focus" to EquipmentRigPreset(
            name = "Split-Diopter De Palma Focus",
            category = "cinema",
            scale = 106.0,
            filters = ClipFilters(contrast = 120.0, saturation = 108.0, sharpen = 28.0, vignette = 26.0),
            rig = CameraRig(swivelY = 4.0, aberration = 3.0)
        )
    )

    // -------------------------------------------------------------------------
    // 4. BUILT-IN STUDIO AUDIO MASTERING GEAR PRESETS
    // -------------------------------------------------------------------------
    val builtinAudioGear: Map<String, AudioGearPreset> = linkedMapOf(
        "Neve 1073 Vocal Warmth" to AudioGearPreset("Neve 1073 Vocal Warmth", listOf(2.0, 3.5, 2.5, 4.0, 3.0), comp = 42.0, pan = 0.0, deess = 30.0, reverb = 8.0, volume = 108.0),
        "SSL 4000G Master Bus Glue" to AudioGearPreset("SSL 4000G Master Bus Glue", listOf(2.5, 1.0, 0.0, 2.5, 3.5), comp = 55.0, pan = 0.0, deess = 15.0, reverb = 5.0, volume = 105.0),
        "Broadcast Podcast Shure SM7B" to AudioGearPreset("Broadcast Podcast Shure SM7B", listOf(-3.0, 2.5, 3.5, 4.5, 2.0), comp = 62.0, pan = 0.0, deess = 48.0, reverb = 0.0, volume = 115.0),
        "Cinema Sub-Bass IMAX Boom" to AudioGearPreset("Cinema Sub-Bass IMAX Boom", listOf(9.0, 5.0, -1.0, 1.0, 2.0), comp = 50.0, pan = 0.0, deess = 0.0, reverb = 18.0, volume = 112.0),
        "Lo-Fi SP-404 Vinyl Cassette" to AudioGearPreset("Lo-Fi SP-404 Vinyl Cassette", listOf(-6.0, 3.0, 4.0, -4.0, -8.0), comp = 38.0, pan = 0.0, deess = 20.0, reverb = 12.0, volume = 96.0),
        "Crisp Dialogue De-Noise" to AudioGearPreset("Crisp Dialogue De-Noise", listOf(-7.0, -2.0, 4.5, 5.0, 2.5), comp = 48.0, pan = 0.0, deess = 55.0, reverb = 0.0, volume = 112.0),
        "Abbey Road Chambers Reverb" to AudioGearPreset("Abbey Road Chambers Reverb", listOf(1.0, 1.5, 2.0, 3.0, 4.0), comp = 30.0, pan = 0.0, deess = 10.0, reverb = 58.0, volume = 100.0),
        "Wide Stereo Synth Mastering" to AudioGearPreset("Wide Stereo Synth Mastering", listOf(4.0, 1.0, -1.0, 3.5, 6.0), comp = 45.0, pan = 0.0, deess = 10.0, reverb = 24.0, volume = 106.0),
        "Club Punch Kick & Highs" to AudioGearPreset("Club Punch Kick & Highs", listOf(7.5, 2.0, -2.5, 4.0, 6.5), comp = 68.0, pan = 0.0, deess = 15.0, reverb = 10.0, volume = 110.0),
        "Documentary Field Lav Mic Fix" to AudioGearPreset("Documentary Field Lav Mic Fix", listOf(-5.0, 1.5, 3.5, 3.0, -1.0), comp = 52.0, pan = 0.0, deess = 40.0, reverb = 0.0, volume = 114.0),
        "Warm Acoustic Ribbon Mic" to AudioGearPreset("Warm Acoustic Ribbon Mic", listOf(1.5, 3.0, 1.0, 2.5, 4.0), comp = 28.0, pan = 0.0, deess = 10.0, reverb = 22.0, volume = 104.0),
        "Telephone / Walkie-Talkie Filter" to AudioGearPreset("Telephone / Walkie-Talkie Filter", listOf(-12.0, -8.0, 8.0, 6.0, -12.0), comp = 75.0, pan = 0.0, deess = 0.0, reverb = 4.0, volume = 102.0)
    )

    val bezierCurvePresets: List<Pair<String, List<Double>>> = listOf(
        "Smooth S" to listOf(0.42, 0.0, 0.58, 1.0),
        "Expo Out" to listOf(0.16, 1.0, 0.3, 1.0),
        "Elastic" to listOf(0.34, 1.56, 0.64, 1.0),
        "Whip Snap" to listOf(0.85, 0.0, 0.15, 1.0),
        "Back Pop" to listOf(0.68, -0.55, 0.27, 1.55),
        "Linear" to listOf(0.0, 0.0, 1.0, 1.0)
    )
}
