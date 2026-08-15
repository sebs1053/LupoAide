package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChestRewardEntity
import com.example.data.local.UserStatsEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPinkAccent
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoPurpleAccent
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoSurfaceVariant
import com.example.ui.theme.LupoTextPrimary
import com.example.ui.theme.LupoTextSecondary

@Composable
fun ProfileScreen(
    stats: UserStatsEntity?,
    chests: List<ChestRewardEntity>,
    onOpenChest: (ChestRewardEntity) -> Unit
) {
    val currentStats = stats ?: UserStatsEntity()
    val level = currentStats.level
    val exp = currentStats.expPoints
    val expNeeded = level * 200
    val progress = (exp.toFloat() / expNeeded.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile & Mascot Avatar Card (Matching Tab 5 Wireframe)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, LupoPurpleAccent, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(LupoPurpleAccent)
                            .border(3.dp, LupoPrimaryGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐺", fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentStats.userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = currentStats.gradeLevel,
                        fontSize = 12.sp,
                        color = LupoCyanNode
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LupoSurfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Estado de Lupo: ", fontSize = 12.sp, color = LupoTextSecondary)
                        Text(
                            text = currentStats.lupoMood,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                    }
                }
            }
        }

        // Gamification Stats & Level Progress
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LupoCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nivel $level - Domador de Estudio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                        Text(
                            text = "$exp / $expNeeded EXP",
                            fontSize = 12.sp,
                            color = LupoTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = LupoPrimaryGold,
                        trackColor = LupoCardBorder
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(icon = "🔥", label = "Racha", value = "${currentStats.streaksCount} Días", color = LupoFlameOrange)
                        StatItem(icon = "⚡", label = "Puntos EXP", value = "${currentStats.expPoints}", color = LupoPrimaryGold)
                        StatItem(icon = "🦴", label = "Huesos", value = "${currentStats.bonesCount}", color = LupoEmeraldGreen)
                        StatItem(icon = "⏱️", label = "Enfoque", value = "${currentStats.totalFocusMinutes} min", color = LupoCyanNode)
                    }
                }
            }
        }

        // Inventory of Chests & Rewards (Cofres con Premios)
        item {
            Text(
                text = "Inventario de Cofres y Recompensas 🎁",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LupoTextPrimary
            )
        }

        items(chests) { chest ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (chest.isOpened) LupoCardBorder else LupoPrimaryGold, RoundedCornerShape(16.dp))
                    .clickable { if (!chest.isOpened) onOpenChest(chest) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (chest.isOpened) LupoSurfaceDark.copy(alpha = 0.5f) else LupoSurfaceDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (chest.isOpened) "📦" else "🎁",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = chest.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Rareza: ${chest.rarity} • +${chest.expBonus} EXP",
                                fontSize = 11.sp,
                                color = LupoCyanNode
                            )
                        }
                    }

                    if (chest.isOpened) {
                        Text(
                            text = "Abierto",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Button(
                            onClick = { onOpenChest(chest) },
                            colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                            modifier = Modifier.testTag("open_chest_button_${chest.id}")
                        ) {
                            Text("Abrir 🎁", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Project Info Banner (Hackathon Jalisco Reto 04)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LupoSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hackathon Jalisco Internacional - Reto 04",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LupoPrimaryGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LupoAide: Educación y Talento. Un ecosistema híbrido que combina educación personalizada con Gemini IA, gestión académica y regulación conductual.",
                        fontSize = 11.sp,
                        color = LupoTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = LupoTextSecondary
        )
    }
}
