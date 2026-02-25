package com.stickytimer.app.core.timer

sealed interface StickyModeState {
    val isEnabled: Boolean
    val phase: StickyPhase
    val remainingSessionMs: Long
    val maxWindowRemainingMs: Long
}

data class StickyModeSnapshot(
    override val isEnabled: Boolean,
    override val phase: StickyPhase,
    override val remainingSessionMs: Long,
    override val maxWindowRemainingMs: Long
) : StickyModeState {
    companion object {
        fun off(): StickyModeSnapshot = StickyModeSnapshot(
            isEnabled = false,
            phase = StickyPhase.OFF,
            remainingSessionMs = 0L,
            maxWindowRemainingMs = 0L
        )
    }
}
