package np.com.bimalkafle.firebaseauthdemoapp.pages


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MasonryGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val spacingPx = with(density) { spacing.roundToPx() }
        val columnWidth = (constraints.maxWidth - spacingPx * (columns - 1)) / columns
        val heights = IntArray(columns) { 0 }

        val placeables = measurables.map { measurable ->
            val p = measurable.measure(
                constraints.copy(minWidth = columnWidth, maxWidth = columnWidth)
            )
            val col = heights.withIndex().minByOrNull { it.value }!!.index
            val x = col * (columnWidth + spacingPx)
            val y = heights[col]
            heights[col] = y + p.height + spacingPx
            Triple(p, x, y)
        }

        val height = heights.maxOrNull()!!.coerceAtMost(constraints.maxHeight)
        layout(constraints.maxWidth, height) {
            placeables.forEach { (p, x, y) -> p.placeRelative(x, y) }
        }
    }
}
