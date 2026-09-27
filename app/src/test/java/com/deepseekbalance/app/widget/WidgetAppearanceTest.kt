package com.deepseekbalance.app.widget

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class WidgetAppearanceTest {
    @Test
    fun `custom image shows image and scrim with light text`() {
        val visuals = WidgetAppearance.visuals(hasCustomImage = true)

        assertTrue(visuals.showImage)
        assertTrue(visuals.showScrim)
        assertEquals(0xFFFFFFFF.toInt(), assertNotNull(visuals.textColors).primary)
    }

    @Test
    fun `default background keeps theme text colors`() {
        val visuals = WidgetAppearance.visuals(hasCustomImage = false)

        assertFalse(visuals.showImage)
        assertFalse(visuals.showScrim)
        assertNull(visuals.textColors)
    }
}
