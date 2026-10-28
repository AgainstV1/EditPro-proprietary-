package com.editpro.studio.model

/**
 * One entry in the clip context menu.
 *
 * The menu is user-configurable: the app stores the ordered list of enabled ids (see
 * `EditorUiState.clipMenuOrder`) and builds the menu from it, so nothing is hardcoded in the UI.
 * [group] is only used to draw separator lines between related blocks of actions.
 */
enum class ClipMenuAction(
    val id: String,
    val label: String,
    val shortcut: String? = null,
    val destructive: Boolean = false,
    val group: Int = 0
) {
    SPLIT("split", "Split at playhead", "S", group = 0),
    KEYFRAME("keyframe", "Add keyframe at playhead", null, group = 0),
    DUPLICATE("duplicate", "Duplicate clip", "Ctrl+D", group = 0),
    COPY_PROPS("copy_props", "Copy clip properties", null, group = 0),
    PASTE_PROPS("paste_props", "Paste clip properties", null, group = 0),
    RESET_PROPS("reset_props", "Reset effects & transform", null, group = 0),

    RIPPLE_DELETE("ripple_delete", "Ripple delete", "Shift+Del", destructive = true, group = 1),
    DELETE("delete", "Delete clip", "Del", destructive = true, group = 1),

    DETECT_BEATS("detect_beats", "Detect audio beats to markers", null, group = 2),
    PRESET_HUB("preset_hub", "Preset & equipment hub", null, group = 2),
    GRADE_SPLIT("grade_split", "Toggle grade split wipe", "Backslash", group = 2),

    MODE_EDIT("mode_edit", "Switch to Normal mode", "Alt+1", group = 3),
    MODE_COLOR("mode_color", "Switch to Colour mode", "Alt+2", group = 3),
    MODE_MOTION("mode_motion", "Switch to Motion mode", "Alt+3", group = 3),
    MODE_MUSIC("mode_music", "Switch to Music mode", "Alt+4", group = 3),

    RIG_ARRI("rig_arri", "Apply ARRI Alexa + Cooke S4 rig", null, group = 4),
    RIG_RED("rig_red", "Apply RED V-Raptor anamorphic rig", null, group = 4),
    AUDIO_SSL("audio_ssl", "Apply SSL master bus audio glue", null, group = 4);

    companion object {
        fun fromId(id: String): ClipMenuAction? = entries.find { it.id == id }

        /**
         * Clip-editing actions are on by default. Mode switches and camera/audio rigs are off,
         * because they are what made the default menu feel like a wall of options.
         */
        val defaultOrder: List<String> = listOf(
            SPLIT.id,
            KEYFRAME.id,
            DUPLICATE.id,
            COPY_PROPS.id,
            PASTE_PROPS.id,
            RESET_PROPS.id,
            RIPPLE_DELETE.id,
            DELETE.id,
            DETECT_BEATS.id,
            PRESET_HUB.id,
            GRADE_SPLIT.id
        )
    }
}
