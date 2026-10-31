package com.editpro.studio.mcp

import android.app.Application
import com.editpro.studio.viewmodel.EditProViewModel

/**
 * Holds the one view model for the whole process.
 *
 * The editor UI and the always-on MCP service must act on the *same* project state, so the view
 * model can no longer be scoped to the activity: it lives here instead. `onCleared` therefore
 * never runs during normal use, which is deliberate — the MCP socket should outlive the UI.
 */
object AgentHost {

    @Volatile
    private var viewModel: EditProViewModel? = null

    fun ensure(application: Application): EditProViewModel =
        viewModel ?: synchronized(this) {
            viewModel ?: EditProViewModel(application).also { viewModel = it }
        }

    fun current(): EditProViewModel? = viewModel
}
