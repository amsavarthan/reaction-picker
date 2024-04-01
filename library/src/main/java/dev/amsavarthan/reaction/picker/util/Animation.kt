package dev.amsavarthan.reaction.picker.util

/**
 * Cubic Bezier curve formula to calculate the position on the curve based on progress.
 */
fun bezierAnimation(progress: Float): Float {
    // Starting point (y = 0)
    val startPoint = 0f

    // Midpoint with negative amplitude (controls the dip)
    val midPoint = -300f

    // Ending point (y = 0)
    val endPoint = 0f

    // Calculate the position on the curve based on progress using cubic Bezier formula
    return (1 - progress) * (1 - progress) * (1 - progress) * startPoint +
            3 * progress * (1 - progress) * (1 - progress) * midPoint +
            3 * progress * progress * (1 - progress) * midPoint +
            progress * progress * progress * endPoint
}