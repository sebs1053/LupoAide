package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoPurpleAccent
import com.example.ui.theme.LupoSurfaceDark

@Composable
fun GamificationTopBar(
    stats: UserStatsEntity?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak = stats?.streaksCount ?: 5
    val exp = stats?.expPoints ?: 420
    val level = stats?.level ?: 3
    val bones = stats?.bonesCount ?: 18

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LupoCanvasDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Circle Indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LupoPurpleAccent)
                .border(1.5.dp, LupoPrimaryGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐺",
                fontSize = 18.sp
            )
        }

        // Gamification Metrics Row (🔥 Streaks, ⚡ EXP, # Level, 🦴 Bones)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricPill(icon = "🔥", value = "$streak", badgeColor = LupoFlameOrange)
            MetricPill(icon = "⚡", value = "$exp", badgeColor = LupoPrimaryGold)
            MetricPill(icon = "Nvl", value = "$level", badgeColor = LupoPrimaryGold)
            MetricPill(icon = "🦴", value = "$bones", badgeColor = LupoEmeraldGreen)
        }
    }
}

@Composable
private fun MetricPill(
    icon: String,
    value: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LupoSurfaceDark)
            .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor
        )
    }
}
