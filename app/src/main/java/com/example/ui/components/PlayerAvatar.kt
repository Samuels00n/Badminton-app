package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalWarmChip

@Composable
fun PlayerAvatar(
    name: String,
    colorHex: String,
    avatarIcon: String = "🏸",
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val parseColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        ForestGreenPrimary
    }

    val isLightBg = try {
        val parsed = android.graphics.Color.parseColor(colorHex)
        val luminance = (0.299 * android.graphics.Color.red(parsed) + 0.587 * android.graphics.Color.green(parsed) + 0.114 * android.graphics.Color.blue(parsed)) / 255.0
        luminance > 0.65
    } catch (e: Exception) {
        false
    }

    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(parseColor)
            .border(1.dp, if (isLightBg) Color(0xFFCCCCCC) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarIcon.isNotBlank() && avatarIcon != "INITIALS") {
            Text(
                text = avatarIcon,
                fontSize = (size.value * 0.52).sp
            )
        } else {
            Text(
                text = if (initials.isNotEmpty()) initials else "?",
                color = if (isLightBg) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4).sp
            )
        }
    }
}
