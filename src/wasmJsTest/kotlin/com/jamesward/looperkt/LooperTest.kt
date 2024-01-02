package com.jamesward.looperkt

import kotlin.test.Test
import kotlin.test.assertTrue

class LooperTest {

    @Test
    fun rgba_should_work() {
        val rgba = RGBA(0xFFEEDDCCu)
        assertTrue(rgba.r.toUInt() == 0xFFu)
        assertTrue(rgba.g.toUInt() == 0xEEu)
        assertTrue(rgba.b.toUInt() == 0xDDu)
        assertTrue(rgba.a.toUInt() == 0xCCu)
        assertTrue(rgba.isVisible)
        assertTrue(!rgba.hide().isVisible)
    }

}