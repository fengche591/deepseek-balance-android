package com.deepseekbalance.app.widget

object WidgetImageSizer {
    const val STORED_IMAGE_MAX_DIMENSION = 2048
    const val WIDGET_IMAGE_MAX_WIDTH = 640
    const val WIDGET_IMAGE_MAX_HEIGHT = 360

    fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
    ): Int {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return 1
        }

        var sampleSize = 1
        while (
            sourceWidth / (sampleSize * 2) >= targetWidth &&
            sourceHeight / (sampleSize * 2) >= targetHeight
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }

    fun fitWithin(
        sourceWidth: Int,
        sourceHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Pair<Int, Int> {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return sourceWidth to sourceHeight
        }
        if (sourceWidth <= maxWidth && sourceHeight <= maxHeight) {
            return sourceWidth to sourceHeight
        }

        val scale = minOf(
            maxWidth.toFloat() / sourceWidth,
            maxHeight.toFloat() / sourceHeight,
        )
        return (sourceWidth * scale).toInt().coerceAtLeast(1) to
            (sourceHeight * scale).toInt().coerceAtLeast(1)
    }
}
