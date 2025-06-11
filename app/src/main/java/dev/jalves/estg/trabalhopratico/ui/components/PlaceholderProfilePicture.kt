package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun PlaceholderProfilePic(
    name: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        if (name.isEmpty())
            "?"
        else
            name.trim()
                .split("\\s+".toRegex())
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
    }

    val backgroundColor = remember(name) {
        val colors = listOf(
            Color(0xFFEF9A9A), Color(0xFFCE93D8), Color(0xFF90CAF9),
            Color(0xFFA5D6A7), Color(0xFFFFCC80), Color(0xFFFFAB91),
            Color(0xFF80DEEA), Color(0xFFB0BEC5)
        )
        colors[(name.hashCode().absoluteValue) % colors.size]
    }

    val fontSize = remember(size) {
        val calculatedSize = size.value * 0.375f
        maxOf(calculatedSize, 10f).sp
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                lineHeight = fontSize
            )
        }
    }
}
