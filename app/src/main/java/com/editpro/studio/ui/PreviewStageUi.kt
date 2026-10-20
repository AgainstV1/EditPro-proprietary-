package com.editpro.studio.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editpro.studio.media.MediaEngine
import com.editpro.studio.model.AssetKind
import com.editpro.studio.model.ClipFilters
import com.editpro.studio.model.ScopesMode
import com.editpro.studio.model.TimelineClip
import com.editpro.studio.model.TimelineEngine
import com.editpro.studio.model.TimelineTrack
import com.editpro.studio.model.WorkspaceMode
import com.editpro.studio.model.formatTimecode
import com.editpro.studio.viewmodel.EditorUiState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PreviewCenterPanel(
    state: EditorUiState,
    onPlayPause: () -> Unit,
    onSeek: (Double) -> Unit,
    onUpdateSelectedClip: ((TimelineClip) -> TimelineClip) -> Unit,
    onSnapHistory: () -> Unit,
    onToggleScopes: () -> Unit,
    onSelectScopeMode: (ScopesMode) -> Unit,
    onToggleGradeSplit: () -> Unit,
    onGradeSplitChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalEditProPalette.current
    val borderAccent = when (state.workspaceMode) {
        WorkspaceMode.COLOR -> palette.orange.copy(alpha = 0.45f)
        WorkspaceMode.MOTION -> palette.purple.copy(alpha = 0.45f)
        WorkspaceMode.MUSIC -> palette.green.copy(alpha = 0.45f)
        WorkspaceMode.EDIT -> Color.White.copy(alpha = 0.14f)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (palette.isLight) Color(0xFFE5E8EE) else Color(0xFF101012))
    ) {
        // Stage Area
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val stageAspect = state.projectSettings.width.toFloat() / max(1, state.projectSettings.height).toFloat()

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(stageAspect, matchHeightConstraintsFirst = maxWidth / maxHeight > stageAspect)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (state.checkerboardOn) Color(0xFF27272B) else Color(0xFF08080A))
                    .border(1.dp, borderAccent, RoundedCornerShape(10.dp))
                    .clipToBounds()
            ) {
                val activeResolved = state.resolvedClips.filter { rc ->
                    val hidden = state.trackStates[rc.clip.track]?.hidden == true
                    !hidden && state.playheadSec >= rc.start && state.playheadSec <= rc.end
                }
                val visuals = activeResolved.filter { it.clip.track.isVisual && it.clip.assetId != null }
                val texts = activeResolved.filter { it.clip.track == TimelineTrack.TEXT }

                if (visuals.isEmpty() && texts.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.clips.isEmpty()) "Import media or tap Demo Reel, then add a clip to the timeline."
                            else "No clip at playhead.",
                            color = Color(0xFF626E82),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "EDIT PRO · NATIVE ANDROID STUDIO",
                            color = Color(0xFF626E82),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // Render Visual Layers (VIDEO 1, VIDEO 2, OVERLAY)
                    for (rc in visuals) {
                        val c = rc.clip
                        val asset = c.assetId?.let { state.assetsById[it] } ?: continue
                        val localT = state.playheadSec - rc.start
                        val anim = TimelineEngine.animatedProps(c, localT)
                        val f = anim.filters

                        val transDur = max(0.05, c.transitionDuration)
                        val transProgress = ((state.playheadSec - rc.start) / transDur).coerceIn(0.0, 1.0)
                        val fadeInAlpha = if (c.transition == "fade" || c.transition == "dissolve") transProgress else 1.0
                        val fadeOutAlpha = if (c.fadeOut > 0.0) ((rc.end - state.playheadSec) / c.fadeOut).coerceIn(0.0, 1.0) else 1.0
                        val finalAlpha = ((anim.opacity / 100.0) * fadeInAlpha * fadeOutAlpha).toFloat().coerceIn(0f, 1f)
                        val squeezeY = (1.0 - min(0.35, c.rig.anamorphic / 100.0)).toFloat()

                        val androidColorMatrix = remember(f) { MediaEngine.buildColorMatrix(f) }
                        val composeColorMatrix = remember(androidColorMatrix) { ColorMatrix(androidColorMatrix.array) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = finalAlpha
                                    val sc = (anim.scale / 100.0).toFloat()
                                    scaleX = sc * (if (c.flipH) -1f else 1f)
                                    scaleY = sc * squeezeY * (if (c.flipV) -1f else 1f)
                                    rotationZ = anim.rotation.toFloat()
                                    rotationX = c.rig.tiltX.toFloat()
                                    rotationY = c.rig.swivelY.toFloat()
                                    translationX = (anim.x / 100.0 * size.width).toFloat()
                                    translationY = (anim.y / 100.0 * size.height).toFloat()
                                    cameraDistance = 12f * density
                                }
                                .clip(RoundedCornerShape(c.cornerRadius.dp))
                        ) {
                            when (asset.kind) {
                                AssetKind.COLOR -> {
                                    val solidColor = try {
                                        Color(android.graphics.Color.parseColor(asset.colorHex ?: "#2678ff"))
                                    } catch (_: Exception) {
                                        Color(0xFF2678FF)
                                    }
                                    Box(modifier = Modifier.fillMaxSize().background(solidColor))
                                }
                                AssetKind.IMAGE, AssetKind.VIDEO -> {
                                    val bmp = remember(asset.localPath) {
                                        asset.localPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
                                    }
                                    if (bmp != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bmp,
                                            contentDescription = c.label,
                                            contentScale = ContentScale.Fit,
                                            colorFilter = ColorFilter.colorMatrix(composeColorMatrix),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF102542), Color(0xFFF87060))
                                                    )
                                                )
                                        ) {
                                            Text(text = c.label, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                AssetKind.AUDIO -> Unit
                            }
                        }
                    }

                    // Vignette Layer
                    val maxVignette = visuals.maxOfOrNull { it.clip.filters.vignette } ?: 0.0
                    if (maxVignette > 0.0) {
                        val vigAlpha = min(0.85, maxVignette / 100.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = vigAlpha))
                                    )
                                )
                        )
                    }

                    // Render Text & Titles Overlays
                    for (rc in texts) {
                        val c = rc.clip
                        val localT = state.playheadSec - rc.start
                        val anim = TimelineEngine.animatedProps(c, localT)
                        val align = when (c.verticalPosition) {
                            "top" -> Alignment.TopCenter
                            "bottom" -> Alignment.BottomCenter
                            else -> Alignment.Center
                        }
                        val txtColor = try {
                            Color(android.graphics.Color.parseColor(c.textColor))
                        } catch (_: Exception) {
                            Color.White
                        }
                        Box(
                            contentAlignment = align,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .graphicsLayer {
                                    alpha = (anim.opacity / 100.0).toFloat().coerceIn(0f, 1f)
                                    val sc = (anim.scale / 100.0).toFloat()
                                    scaleX = sc
                                    scaleY = sc
                                    rotationZ = anim.rotation.toFloat()
                                    translationX = (anim.x / 100.0 * size.width).toFloat()
                                    translationY = (anim.y / 100.0 * size.height).toFloat()
                                }
                        ) {
                            Text(
                                text = c.text.ifEmpty { "Text" },
                                color = txtColor,
                                fontSize = (c.fontSize * 0.48f).coerceIn(12f, 64f).sp,
                                fontWeight = if (c.fontWeight >= 700) FontWeight.ExtraBold else FontWeight.Medium,
                                textAlign = when (c.textAlign) {
                                    "left" -> TextAlign.Start
                                    "right" -> TextAlign.End
                                    else -> TextAlign.Center
                                }
                            )
                        }
                    }

                    // Interactive Free-Framing Blue Transform Box for Selected Visual Clip
                    val selClip = state.selectedClip
                    if (selClip != null && selClip.track.isVisual && visuals.any { it.clip.id == selClip.id }) {
                        InteractivePreviewTransformBox(
                            clip = selClip,
                            onSnapHistory = onSnapHistory,
                            onUpdateClip = onUpdateSelectedClip
                        )
                    }
                }

                // Safe-Area Guides Overlay
                if (state.safeGuidesOn) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.55f),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        )
                        drawLine(Color.White.copy(alpha = 0.25f), Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), 1f)
                        drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), 1f)
                    }
                }

                // Motion Mode Path & Rule-of-Thirds HUD
                if (state.workspaceMode == WorkspaceMode.MOTION) {
                    MotionHudCanvas(selectedClip = state.selectedClip)
                }

                // Split-Screen Before/After Grade Wipe Line
                if (state.gradeSplitOn) {
                    GradeSplitWipeHandle(
                        splitPercent = state.gradeSplitPercent,
                        onSplitChange = onGradeSplitChange
                    )
                }

                // UI Zoom HUD pill in bottom-right of stage
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .background(Color(0xDD151519), RoundedCornerShape(7.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "UI ${(state.uiZoom * 100).roundToInt()}%",
                        color = Color(0xFFDDDDDD),
                        fontSize = 10.sp
                    )
                }
            }

            // Floating Live Video Scopes Dock (Colour Mode or Pinned)
            if (state.workspaceMode == WorkspaceMode.COLOR || state.scopesForceOn) {
                LiveVideoScopesDock(
                    mode = state.scopesMode,
                    filters = state.selectedClip?.filters ?: ClipFilters(),
                    playheadSec = state.playheadSec,
                    onSelectMode = onSelectScopeMode,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }

            // Music Mode Live Spectrum & LUFS Dock
            if (state.workspaceMode == WorkspaceMode.MUSIC) {
                MusicSpectrumMeterDock(
                    clip = state.selectedClip,
                    isPlaying = state.isPlaying,
                    playheadSec = state.playheadSec,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }

        // Transport Controls Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(palette.panel.copy(alpha = 0.85f))
                .border(0.5.dp, palette.line)
                .padding(horizontal = 12.dp)
        ) {
            TransportButton(label = "|◀", tooltip = "Go to start") { onSeek(0.0) }
            Spacer(modifier = Modifier.width(6.dp))
            TransportButton(label = "−1s", tooltip = "Back one second") { onSeek(state.playheadSec - 1.0) }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
                    .clickable(onClick = onPlayPause)
            ) {
                Text(
                    text = if (state.isPlaying) "Ⅱ" else "▶",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TransportButton(label = "+1s", tooltip = "Forward one second") { onSeek(state.playheadSec + 1.0) }
            Spacer(modifier = Modifier.width(6.dp))
            TransportButton(label = "▶|", tooltip = "Go to end") { onSeek(state.durationSec) }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${formatTimecode(state.playheadSec)} / ${formatTimecode(state.durationSec)}",
                color = palette.muted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(136.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            SmileIconButton(
                icon = SmileIconType.SCOPES,
                accessibleLabel = "Scopes",
                isActive = state.scopesForceOn || state.workspaceMode == WorkspaceMode.COLOR,
                onClick = onToggleScopes
            )
            Spacer(modifier = Modifier.width(6.dp))
            SmileIconButton(
                icon = SmileIconType.SPLIT_WIPE,
                accessibleLabel = "Split Grade",
                isActive = state.gradeSplitOn,
                onClick = onToggleGradeSplit
            )
        }
    }
}

@Composable
private fun TransportButton(label: String, tooltip: String, onClick: () -> Unit) {
    val palette = LocalEditProPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(palette.controlBg)
            .clickable(onClick = onClick)
    ) {
        Text(text = label, color = palette.text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InteractivePreviewTransformBox(
    clip: TimelineClip,
    onSnapHistory: () -> Unit,
    onUpdateClip: ((TimelineClip) -> TimelineClip) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val stageW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val stageH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val scalePct = clip.scale.coerceIn(10.0, 250.0).toFloat() / 100f
        val boxW = (stageW * scalePct).coerceIn(48f, stageW)
        val boxH = (stageH * scalePct).coerceIn(36f, stageH)
        val offsetX = ((stageW - boxW) / 2f + (clip.x.toFloat() / 100f) * stageW).roundToInt()
        val offsetY = ((stageH - boxH) / 2f + (clip.y.toFloat() / 100f) * stageH).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX, offsetY) }
                .size(
                    width = (boxW / (stageW / maxWidth.value)).dp,
                    height = (boxH / (stageH / maxHeight.value)).dp
                )
                .border(1.dp, Color(0xFF0A84FF))
                .background(Color(0x140A84FF))
                .pointerInput(clip.id) {
                    detectDragGestures(
                        onDragStart = { onSnapHistory() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dxPct = (dragAmount.x / stageW) * 100.0
                            val dyPct = (dragAmount.y / stageH) * 100.0
                            onUpdateClip { cur ->
                                var nx = (cur.x + dxPct).coerceIn(-100.0, 100.0)
                                var ny = (cur.y + dyPct).coerceIn(-100.0, 100.0)
                                if (abs(nx) < 1.5) nx = 0.0
                                if (abs(ny) < 1.5) ny = 0.0
                                cur.copy(x = nx, y = ny)
                            }
                        }
                    )
                }
        ) {
            Text(
                text = "DRAG TO POSITION",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color(0xFF0A84FF), RoundedCornerShape(bottomEnd = 4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
            // Resize handle in bottom-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .background(Color(0xFF0A84FF), RoundedCornerShape(3.dp))
                    .border(1.5.dp, Color.White, RoundedCornerShape(3.dp))
                    .pointerInput(clip.id) {
                        detectDragGestures(
                            onDragStart = { onSnapHistory() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val deltaScale = (dragAmount.x / stageW) * 200.0
                                onUpdateClip { cur ->
                                    cur.copy(scale = (cur.scale + deltaScale).coerceIn(10.0, 250.0))
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
private fun GradeSplitWipeHandle(
    splitPercent: Float,
    onSplitChange: (Float) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val xPx = (w * (splitPercent / 100f)).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(xPx, 0) }
                .fillMaxHeight()
                .width(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val nextPct = splitPercent + (dragAmount.x / w) * 100f
                        onSplitChange(nextPct)
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFF9F0A))
            )
            Text(
                text = "GRADE ◀ ▶ RAW",
                color = Color(0xFFFF9F0A),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .background(Color(0xE0141418), RoundedCornerShape(99.dp))
                    .border(0.5.dp, Color(0xFFFF9F0A), RoundedCornerShape(99.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MotionHudCanvas(selectedClip: TimelineClip?) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gridColor = Color(0x38BF5AF2)
        listOf(1f / 3f, 2f / 3f).forEach { p ->
            drawLine(gridColor, Offset(w * p, 0f), Offset(w * p, h), 1f)
            drawLine(gridColor, Offset(0f, h * p), Offset(w, h * p), 1f)
        }
        val ks = selectedClip?.keyframes?.sortedBy { it.time } ?: emptyList()
        if (ks.isNotEmpty()) {
            val path = Path()
            ks.forEachIndexed { idx, k ->
                val px = w * (0.5f + (k.x.toFloat() / 100f) * 0.5f)
                val py = h * (0.5f + (k.y.toFloat() / 100f) * 0.5f)
                if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, Color(0xFFFFD60A), style = Stroke(width = 2.dp.toPx()))
            ks.forEach { k ->
                val px = w * (0.5f + (k.x.toFloat() / 100f) * 0.5f)
                val py = h * (0.5f + (k.y.toFloat() / 100f) * 0.5f)
                drawCircle(Color(0xFFFFD60A), radius = 4.5.dp.toPx(), center = Offset(px, py))
            }
        }
    }
}

@Composable
private fun LiveVideoScopesDock(
    mode: ScopesMode,
    filters: ClipFilters,
    playheadSec: Double,
    onSelectMode: (ScopesMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalEditProPalette.current
    Column(
        modifier = modifier
            .width(232.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xDC141418))
            .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(11.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "LIVE SCOPES",
                color = palette.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ScopesMode.entries.forEach { sm ->
                    val active = sm == mode
                    Text(
                        text = sm.label,
                        color = if (active) Color.White else palette.muted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (active) palette.accent else Color.Transparent)
                            .clickable { onSelectMode(sm) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF08090C))
        ) {
            val w = size.width
            val h = size.height
            listOf(0.2f, 0.5f, 0.8f).forEach { p ->
                drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, h * p), Offset(w, h * p), 1f)
            }
            val br = (filters.brightness / 100.0).toFloat()
            val ct = (filters.contrast / 100.0).toFloat()
            val sat = (filters.saturation / 100.0).toFloat()
            val rShift = ((filters.liftR + filters.gammaR + filters.gainR) / 60.0 + filters.temperature / 400.0).toFloat()
            val gShift = ((filters.liftG + filters.gammaG + filters.gainG) / 60.0 - filters.tint / 500.0).toFloat()
            val bShift = ((filters.liftB + filters.gammaB + filters.gainB) / 60.0 - filters.temperature / 400.0).toFloat()

            when (mode) {
                ScopesMode.PARADE -> {
                    val third = w / 3f
                    val colors = listOf(Color(0x99FF453A), Color(0x9930D158), Color(0xAA0A84FF))
                    val shifts = listOf(rShift, gShift, bShift)
                    for (ch in 0..2) {
                        for (sx in 0 until 48) {
                            val u = sx / 48f
                            val px = ch * third + u * (third - 6f) + 3f
                            val wave = (0.5f + 0.32f * sin(u * 6.28f + playheadSec.toFloat() * 2f))
                            val valNorm = (((wave - 0.5f) * ct + 0.5f) * br + shifts[ch] * sat).coerceIn(0.04f, 0.96f)
                            val py = h - 4f - valNorm * (h - 8f)
                            drawRect(colors[ch], Offset(px, py - 8f), Size(2.2f, 16f))
                        }
                    }
                }
                ScopesMode.WAVEFORM -> {
                    for (sx in 0 until 64) {
                        val u = sx / 64f
                        val px = u * (w - 6f) + 3f
                        val wave = (0.5f + 0.35f * sin(u * 6.28f + playheadSec.toFloat() * 1.5f) * cos(u * 3.14f))
                        val luma = (((wave - 0.5f) * ct + 0.5f) * br).coerceIn(0.05f, 0.95f)
                        val py = h - 4f - luma * (h - 8f)
                        drawRect(Color(0x9964D2FF), Offset(px, py - 6f), Size(2.5f, 12f))
                    }
                }
                ScopesMode.HISTOGRAM -> {
                    val strokes = listOf(Color(0xFFFF453A), Color(0xFF30D158), Color(0xFF0A84FF))
                    val shifts = listOf(rShift, gShift, bShift)
                    for (ch in 0..2) {
                        val path = Path()
                        for (b in 0..36) {
                            val u = b / 36f
                            val center = (0.5f + shifts[ch] * 0.5f).coerceIn(0.15f, 0.85f)
                            val dist = (u - center) * (2.5f / max(0.3f, ct))
                            val amp = kotlin.math.exp((-dist * dist * 4f).toDouble()).toFloat() * br.coerceIn(0.4f, 1.3f)
                            val x = 4f + u * (w - 8f)
                            val y = h - 4f - amp.coerceIn(0f, 0.95f) * (h - 12f)
                            if (b == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, strokes[ch], style = Stroke(width = 1.6.dp.toPx()))
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicSpectrumMeterDock(
    clip: TimelineClip?,
    isPlaying: Boolean,
    playheadSec: Double,
    modifier: Modifier = Modifier
) {
    val eq = clip?.audioEq ?: listOf(0.0, 0.0, 0.0, 0.0, 0.0)
    val vol = ((clip?.volume ?: 100.0) / 100.0).coerceAtLeast(0.1)
    val lufs = -18.0 + log10(vol) * 14.0 + eq.sum() * 0.15

    Column(
        modifier = modifier
            .width(244.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xDC141418))
            .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(11.dp))
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MASTER BUS · EQ & LUFS", color = Color(0xFF9A9AA3), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
            Text("${"%.1f".format(lufs)} LUFS", color = Color(0xFF30D158), fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF08090C))
        ) {
            val w = size.width
            val h = size.height
            val bars = 24
            val bw = (w - 36f) / bars
            for (i in 0 until bars) {
                val bandIdx = min(4, i / 5)
                val eqBoost = ((eq.getOrElse(bandIdx) { 0.0 } + 12.0) / 24.0).toFloat()
                val wave = if (isPlaying) {
                    0.35f + 0.55f * abs(sin(playheadSec.toFloat() * 11f + i * 0.7f) * cos(playheadSec.toFloat() * 7f - i * 0.4f))
                } else 0.22f
                val amp = (wave * vol.toFloat() * (0.6f + eqBoost * 0.7f)).coerceIn(0.08f, 0.95f)
                val bh = amp * (h - 10f)
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFF453A), Color(0xFFFFD60A), Color(0xFF30D158))),
                    topLeft = Offset(4f + i * bw, h - 4f - bh),
                    size = Size(max(2f, bw - 2f), bh)
                )
            }
            // Stereo L/R vertical VU bars
            for (ch in 0..1) {
                val x = w - 26f + ch * 11f
                val level = ((if (isPlaying) 0.68f else 0.28f) * vol.toFloat() * (if (ch == 0) 1f else 0.94f)).coerceIn(0.08f, 0.95f)
                val bh = level * (h - 12f)
                drawRect(Color(0xFF30D158), Offset(x, h - 4f - bh), Size(8f, bh))
            }
        }
    }
}
