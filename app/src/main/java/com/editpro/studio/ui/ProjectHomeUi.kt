package com.editpro.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editpro.studio.model.formatTimecode
import com.editpro.studio.viewmodel.EditorUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lightweight landing screen shown before the editor.
 *
 * Honest about the platform: an Android app cannot browse the user's storage on its own,
 * so "recent projects" is a record of what was saved through the picker — not a live scan
 * of the file system. Opening always goes through the system file picker.
 */
@Composable
fun ProjectHomeScreen(
    state: EditorUiState,
    onNewProject: () -> Unit,
    onOpenProject: () -> Unit,
    onContinue: () -> Unit,
    onRemoveRecent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalEditProPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(palette.panel)
                .border(0.5.dp, palette.line, RoundedCornerShape(18.dp))
                .padding(24.dp)
        ) {
            AppWordmark(title = "Edit Pro Studio")
            Text("Your projects", color = palette.text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Android does not let an app browse your storage by itself, so this list is a " +
                    "record of the projects you have saved — the files stay wherever you put them. " +
                    "“Open” always asks you to pick the file.",
                color = palette.muted,
                fontSize = 12.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InspectorActionButton(
                    text = "＋ New project",
                    isPrimary = true,
                    modifier = Modifier.weight(1f),
                    onClick = onNewProject
                )
                InspectorActionButton(
                    text = "Open project…",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenProject
                )
            }

            if (state.clips.isNotEmpty() || state.assets.isNotEmpty()) {
                InspectorActionButton(
                    text = "Continue current project  ·  ${state.clips.size} clip(s)",
                    onClick = onContinue
                )
            }

            SectionHeader("RECENT")
            if (state.recentProjects.isEmpty()) {
                Text(
                    "Nothing saved yet. Create a project, then use Save to record it here.",
                    color = palette.muted,
                    fontSize = 12.sp
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    state.recentProjects.forEach { rp ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.controlBg)
                                .padding(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rp.name,
                                    color = palette.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${rp.clipCount} clips · ${formatTimecode(rp.durationSec)} · saved ${formatRecentTime(rp.savedAtEpochMs)}",
                                    color = palette.muted,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            InspectorActionButton(
                                text = "Open…",
                                modifier = Modifier.width(88.dp),
                                onClick = onOpenProject
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            InspectorActionButton(
                                text = "Remove",
                                modifier = Modifier.width(92.dp),
                                onClick = { onRemoveRecent(rp.name) }
                            )
                        }
                    }
                }
                Text(
                    "Remove only clears this entry — it never deletes your saved file.",
                    color = palette.muted,
                    fontSize = 10.5.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            InspectorActionButton(text = "Enter editor  ▶", onClick = onContinue)
        }
    }
}

private fun formatRecentTime(epochMs: Long): String {
    if (epochMs <= 0L) return "earlier"
    val minutes = (System.currentTimeMillis() - epochMs) / 60_000L
    return when {
        minutes < 1L -> "just now"
        minutes < 60L -> "${minutes}m ago"
        minutes < 1_440L -> "${minutes / 60L}h ago"
        minutes < 10_080L -> "${minutes / 1_440L}d ago"
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
    }
}
