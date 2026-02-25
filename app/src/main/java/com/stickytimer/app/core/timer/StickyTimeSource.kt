package com.stickytimer.app.core.timer

interface StickyTimeSource {
    fun nowMs(): Long
}

object SystemStickyTimeSource : StickyTimeSource {
    override fun nowMs(): Long = System.currentTimeMillis()
}
