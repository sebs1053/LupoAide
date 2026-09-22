package com.example.lupoaide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.lupoaide.R
import com.example.lupoaide.data.local.UserProfileEntity

data class LupoOutfit(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val icon: ImageVector,
    val color: Color,
    val badge: String,
    val imageRes: Int? = null
)

val LUPO_OUTFITS = listOf(
    LupoOutfit(
        id = "default",
        name = "Lupo Clásico",
        description = "El tierno lobito gris fiel que te acompaña en tus primeros pasos de estudio.",
        cost = 0,
        icon = Icons.Default.Pets,
        color = Color(0xFF6366F1),
        badge = "🐺 Base",
        imageRes = R.drawable.lupo_skin_base
    ),
    LupoOutfit(
        id = "lupo_67",
        name = "Lupo 67 (Six-Seven)",
        description = "Modo sobrecargado con aura verde relampagueante y boost +67 de concentración.",
        cost = 100,
        icon = Icons.Default.ElectricBolt,
        color = Color(0xFF10B981),
        badge = "⚡ Six-Seven",
        imageRes = R.drawable.lupo_skin_67
    ),
    LupoOutfit(
        id = "lupo_sayayin",
        name = "Lupo Súper Sayayin",
        description = "¡Poder ki al máximo! Aura dorada llameante de estudio para devorar libros y exámenes.",
        cost = 150,
        icon = Icons.Default.FlashOn,
        color = Color(0xFFEAB308),
        badge = "🔥 Súper Sayayin",
        imageRes = R.drawable.lupo_skin_sayayin
    ),
    LupoOutfit(
        id = "scholar",
        name = "Lupo Graduado",
        description = "Con toga y birrete académico listo para honores de excelencia.",
        cost = 80,
        icon = Icons.Default.School,
        color = Color(0xFF0284C7),
        badge = "🎓 Académico"
    ),
    LupoOutfit(
        id = "scientist",
        name = "Lupo Científico",
        description = "Bata blanca y gafas de laboratorio para dominar fórmulas y experimentos.",
        cost = 120,
        icon = Icons.Default.Science,
        color = Color(0xFF10B981),
        badge = "🔬 Ciencia"
    ),
    LupoOutfit(
        id = "coder",
        name = "Lupo Developer",
        description = "Capucha tech, visor cyberpunk y café para sesiones de programación.",
        cost = 150,
        icon = Icons.Default.Terminal,
        color = Color(0xFF8B5CF6),
        badge = "💻 Hacker"
    ),
    LupoOutfit(
        id = "artist",
        name = "Lupo Creativo",
        description = "Boina artística y paleta de ideas para proyectos de diseño y humanidades.",
        cost = 100,
        icon = Icons.Default.Palette,
        color = Color(0xFFEC4899),
        badge = "🎨 Arte"
    ),
    LupoOutfit(
        id = "focus_master",
        name = "Lupo Modo Zen",
        description = "Aura dorada de máxima concentración inmune a cualquier distracción.",
        cost = 200,
        icon = Icons.Default.Bolt,
        color = Color(0xFFF59E0B),
        badge = "⚡ Enfoque"
    ),
    LupoOutfit(
        id = "king",
        name = "Lupo Rey del Estudio",
        description = "Corona de oro real y capa imperial para los mejores promedios.",
        cost = 300,
        icon = Icons.Default.WorkspacePremium,
        color = Color(0xFFD97706),
        badge = "👑 Élite"
    )
)

@Composable
fun LupoShopModal(
    profile: UserProfileEntity?,
    onBuyAndEquip: (outfitId: String, cost: Int) -> Unit,
    onEquip: (outfitId: String) -> Unit,
    onBuyStreakFreeze: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val coins = profile?.coins ?: 0
    val activeOutfitId = profile?.activeOutfitId ?: "default"
    val streakFreezes = profile?.streakFreezes ?: 0
    val unlockedSet = remember(profile?.unlockedOutfits) {
        (profile?.unlockedOutfits ?: "default").split(",").map { it.trim() }.toSet()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("lupo_shop_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tienda de Lupo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Skins y potenciadores de estudio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Saldo de Monedas Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🪙",
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$coins Monedas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Gana más completando tareas y lecciones",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Potenciador: Congelador de Racha (Streak Freeze)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("streak_freeze_shop_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = "Congelador de Racha",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Congelador de Racha",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0284C7)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Tienes: $streakFreezes",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Salva tu racha automáticamente si olvidas estudiar un día.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onBuyStreakFreeze,
                            enabled = coins >= 50,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("🪙 50", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid de Atuendos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(LUPO_OUTFITS) { outfit ->
                        val isUnlocked = unlockedSet.contains(outfit.id) || outfit.cost == 0
                        val isEquipped = activeOutfitId == outfit.id
                        val canAfford = coins >= outfit.cost

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isEquipped) outfit.color.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, outfit.color) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Badge & Icon / Image
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(outfit.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (outfit.imageRes != null) {
                                        Image(
                                            painter = painterResource(id = outfit.imageRes),
                                            contentDescription = outfit.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = outfit.icon,
                                            contentDescription = outfit.name,
                                            tint = outfit.color,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = outfit.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = outfit.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                when {
                                    isEquipped -> {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = outfit.color
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Equipado",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    isUnlocked -> {
                                        Button(
                                            onClick = { onEquip(outfit.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Equipar", fontSize = 12.sp)
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = { onBuyAndEquip(outfit.id, outfit.cost) },
                                            enabled = canAfford,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🪙 ${outfit.cost}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
