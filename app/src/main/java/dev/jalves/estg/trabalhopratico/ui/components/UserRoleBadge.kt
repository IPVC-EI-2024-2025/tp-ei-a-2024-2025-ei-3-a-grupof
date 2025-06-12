package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import dev.jalves.estg.trabalhopratico.objects.Role

@Composable
fun UserRoleBadge(role: Role) {
    Box(
        modifier = Modifier
            .background(color = role.color, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            stringResource(role.descriptionId), style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(
                    ColorUtils.blendARGB(
                        role.color.toArgb(),
                        Color.Black.toArgb(),
                        0.88f
                    )
                )
            )
        )
    }
}