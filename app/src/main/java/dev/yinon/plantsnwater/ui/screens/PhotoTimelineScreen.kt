package dev.yinon.plantsnwater.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun PhotoTimelineScreen(plantId: Long) {
    ScreenColumn {
        SectionTitle("Photo timeline")
        EmptyState(
            "Local photos are next",
            "The data model is ready for dated growth photos, notes, stages, and local media export."
        )
    }
}
