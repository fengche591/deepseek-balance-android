package com.deepseekbalance.app.widget

data class WidgetTextColors(
    val primary: Int,
    val muted: Int,
    val error: Int,
)

data class WidgetVisuals(
    val showImage: Boolean,
    val showScrim: Boolean,
    val textColors: WidgetTextColors?,
)

object WidgetAppearance {
    private val customImageColors = WidgetTextColors(
        primary = 0xFFFFFFFF.toInt(),
        muted = 0xE6FFFFFF.toInt(),
        error = 0xFFFFB4AB.toInt(),
    )

    fun visuals(hasCustomImage: Boolean): WidgetVisuals {
        return WidgetVisuals(
            showImage = hasCustomImage,
            showScrim = hasCustomImage,
            textColors = customImageColors.takeIf { hasCustomImage },
        )
    }
}
