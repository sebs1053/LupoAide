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
import com.example.lupoaide.data.local.UserProfileEntity

data class LupoOutfit(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val icon: ImageVector,
    val color: Color,
    val badge: String
)

val LUPO_OUTFITS = listOf(
    LupoOutfit(
        id = "default",
        name = "Lupo Clásico",
        description = "El fiel lobito gris que te acompaña en tus primeros pasos de estudio.",
        cost = 0,
        icon = Icons.Default.Pets,
        color = Color(0xFF6366F1),
        badge = "🐺 Básico"
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
    onDismiss: () -> Unit
) {
    val coins = profile?.coins ?: 0
    val activeOutfitId = profile?.activeOutfitId ?: "default"
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
                                text = "Desbloquea skins con tus monedas",
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

                Spacer(modifier = Modifier.height(14.dp))

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
                                // Badge & Icon
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(outfit.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = outfit.icon,
                                        contentDescription = outfit.name,
                                        tint = outfit.color,
                                        modifier = Modifier.size(32.dp)
                                    )
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
