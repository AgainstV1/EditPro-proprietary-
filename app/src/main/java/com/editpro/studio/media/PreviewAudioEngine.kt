package com.editpro.studio.media

import android.media.MediaPlayer
import com.editpro.studio.model.MediaAsset
import com.editpro.studio.model.ResolvedClip
import com.editpro.studio.model.TimelineClip
import com.editpro.studio.model.TimelineEngine
import com.editpro.studio.model.TimelineTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Plays the timeline's audio clips during preview, roughly in sync with the playhead.
 *
 * Preview only. Each audio clip gets its own `MediaPlayer`, started at the correct source
 * offset (honouring trim and the current playhead), with per-clip volume, fades and volume
 * automation applied as playback advances. Mute/solo is resolved by the caller.
 *
 * Every operation is defensive: a codec that refuses a file, a missing media file or a player
 * that dies mid-clip degrades to silence rather than interrupting editing.
 *
 * The exported MP4's audio is a completely separate pipeline (see NativeVideoExporter).
 */
class PreviewAudioEngine {

    private val active = mutableListOf<Pair<MediaPlayer, TimelineClip>>()
    private var scheduler: Job? = null

    fun stop() {
        scheduler?.cancel()
        scheduler = null
        active.forEach { (p, _) -> release(p) }
        active.clear()
    }

    /**
     * Starts every audible clip that is still ahead of (or under) [fromSec].
     *
     * @param audibleTracks audio tracks that should be heard — mute/solo already resolved.
     */
    fun start(
        scope: CoroutineScope,
        clips: List<ResolvedClip>,
        assets: Map<String, MediaAsset>,
        audibleTracks: Set<TimelineTrack>,
        fromSec: Double
    ) {
        stop()
        scheduler = scope.launch {
            for (rc in clips) {
                val clip = rc.clip
                if (!clip.track.isAudio || clip.track !in audibleTracks) continue
                if (rc.end <= fromSec) continue

                val asset = clip.assetId?.let { assets[it] } ?: continue
                val path = asset.localPath ?: continue
                if (!File(path).exists()) continue

                // Clips that begin later wait for their cue.
                val delayMs = ((rc.start - fromSec) * 1000.0).toLong()
                if (delayMs > 0L) delay(delayMs)

                val offsetSec = clip.trimIn + (fromSec - rc.start).coerceAtLeast(0.0)
                val player = MediaPlayer()
                try {
                    player.setDataSource(path)
                    player.prepare()
                    player.seekTo((offsetSec * 1000.0).toLong().toInt().coerceAtLeast(0))
                    val gain = gainFor(clip, (fromSec - rc.start).coerceAtLeast(0.0), rc.duration)
                    player.setVolume(gain, gain)
                    player.start()
                    active.add(player to clip)
                } catch (e: Exception) {
                    release(player)
                }
            }
        }
    }

    /**
     * Called from the playback ticker: keeps fades/automation audible and reaps finished players.
     * Cheap enough to run a few times a second.
     */
    fun updateVolumes(clips: List<ResolvedClip>, playheadSec: Double) {
        val iterator = active.iterator()
        while (iterator.hasNext()) {
            val (player, clip) = iterator.next()
            val rc = clips.firstOrNull { it.clip.id == clip.id }
            if (rc == null || playheadSec > rc.end) {
                release(player)
                iterator.remove()
                continue
            }
            if (playheadSec < rc.start) continue
            val gain = gainFor(clip, playheadSec - rc.start, rc.duration)
            runCatching { player.setVolume(gain, gain) }
        }
    }

    private fun gainFor(clip: TimelineClip, localSec: Double, durationSec: Double): Float {
        val base = (clip.volume / 100.0).coerceIn(0.0, 2.0)
        val envelope = TimelineEngine.audioEnvelope(clip, localSec, durationSec)
        return (base * envelope).coerceIn(0.0, 1.0).toFloat()
    }

    private fun release(player: MediaPlayer) {
        runCatching { if (player.isPlaying) player.stop() }
        runCatching { player.reset() }
        runCatching { player.release() }
    }
}
