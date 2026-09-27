package com.deepseekbalance.app.widget

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class WidgetImageSizerTest {
    @Test
    fun `large image is sampled before decoding`() {
        val sampleSize = WidgetImageSizer.calculateInSampleSize(
            sourceWidth = 4096,
            sourceHeight = 3072,
            targetWidth = 640,
            targetHeight = 360,
        )

        assertTrue(sampleSize >= 4)
    }

    @Test
    fun `small image is not sampled`() {
        val sampleSize = WidgetImageSizer.calculateInSampleSize(
            sourceWidth = 320,
            sourceHeight = 180,
            targetWidth = 640,
            targetHeight = 360,
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun `landscape image is fitted without distortion`() {
        val result = WidgetImageSizer.fitWithin(
            sourceWidth = 2048,
            sourceHeight = 1536,
            maxWidth = 640,
            maxHeight = 360,
        )

        assertEquals(480 to 360, result)
    }

    @Test
    fun `small image is not upscaled`() {
        val result = WidgetImageSizer.fitWithin(
            sourceWidth = 320,
            sourceHeight = 180,
            maxWidth = 640,
            maxHeight = 360,
        )

        assertEquals(320 to 180, result)
    }
}
