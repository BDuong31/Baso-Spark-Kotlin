package basostudio.basospark.ui.modifiers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Một Modifier tùy chỉnh để tái tạo hiệu ứng nền từ file auth.xml.
 * Modifier này nhận vào các màu sắc để có thể thích ứng với Dark/Light Mode.
 *
 * @param backgroundColor Màu nền chính của Box.
 * @param gradientColors Danh sách các màu cho các dải gradient.
 */
fun Modifier.authBackground(
    backgroundColor: Color,
    gradientColors: List<Color>
): Modifier = this.drawBehind {
    // Lớp nền chính
    drawRect(color = backgroundColor)

    // Đảm bảo chúng ta có đủ màu cho 5 dải gradient
    if (gradientColors.size < 5) return@drawBehind

    val radius = 200.dp.toPx()

    // Gradient 1 (dưới, trái)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gradientColors[0], Color.Transparent),
            center = Offset(x = size.width * 0.37f, y = size.height * 1.0f),
            radius = radius
        ),
        radius = radius,
        center = Offset(x = size.width * 0.37f, y = size.height * 1.0f)
    )

    // Gradient 2 (dưới, phải)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gradientColors[1], Color.Transparent),
            center = Offset(x = size.width * 0.61f, y = size.height * 1.0f),
            radius = radius
        ),
        radius = radius,
        center = Offset(x = size.width * 0.61f, y = size.height * 1.0f)
    )

    // Gradient 3 (trên, trái)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gradientColors[2], Color.Transparent),
            center = Offset(x = size.width * 0.21f, y = 0f),
            radius = radius
        ),
        radius = radius,
        center = Offset(x = size.width * 0.21f, y = 0f)
    )

    // Gradient 4 (trên, giữa)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gradientColors[3], Color.Transparent),
            center = Offset(x = size.width * 0.45f, y = 0f),
            radius = radius
        ),
        radius = radius,
        center = Offset(x = size.width * 0.45f, y = 0f)
    )

    // Gradient 5 (trên, phải)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gradientColors[4], Color.Transparent),
            center = Offset(x = size.width * 0.60f, y = 0f),
            radius = radius
        ),
        radius = radius,
        center = Offset(x = size.width * 0.60f, y = 0f)
    )
}

/**
 * Một Composable tiện ích để lấy bộ màu cho nền Auth dựa trên theme hiện tại.
 */
@Composable
fun authGradientColors(): List<Color> {
    return if (isSystemInDarkTheme()) {
        listOf(
            Color(0x303B3BBA),
            Color(0x2B623D76),
            Color(0x126F5478),
            Color(0x1A7400AD),
            Color(0x17D9E000)
        )
    } else {
        listOf(
            Color(0x30A1A1F0),
            Color(0x2B9C72B5),
            Color(0x12A988B7),
            Color(0x1AB400FF),
            Color(0x17FFF84D)
        )
    }
}