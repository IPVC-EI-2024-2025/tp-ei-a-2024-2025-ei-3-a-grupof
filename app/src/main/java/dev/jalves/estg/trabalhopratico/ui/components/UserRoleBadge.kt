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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils

// Temporary
// Should be replaced with database roles
enum class UserRole(val color: Color, val description: String) {
    Employee(Color.hsl(119f, 0.75f, 0.75f), "Employee"),
    Admin(Color.hsl(40f, 0.75f, 0.75f), "Admin"),
    Manager(Color.hsl(280f, 0.75f, 0.75f), "Manager")
}


@Composable
fun UserRoleBadge(role: UserRole) {
    Box(
        modifier = Modifier
            .background(color = role.color, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            role.description, style = TextStyle(
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