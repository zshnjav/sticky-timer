package com.stickytimer.app.feature.sticky

import com.stickytimer.app.core.timer.StickyModeSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StickyModeStatusStore {
    private val mutableState = MutableStateFlow(StickyModeSnapshot.off())
    val state: StateFlow<StickyModeSnapshot> = mutableState.asStateFlow()

    fun publish(snapshot: StickyModeSnapshot) {
        mutableState.value = snapshot
    }
}
