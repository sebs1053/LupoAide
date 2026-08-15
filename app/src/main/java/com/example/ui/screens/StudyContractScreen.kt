package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BlacklistedAppEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoPurpleAccent
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoTextPrimary
import com.example.ui.theme.LupoTextSecondary

@Composable
fun StudyContractScreen(
    blacklistedApps: List<BlacklistedAppEntity>,
    isContractActive: Boolean,
    contractMinutes: Int,
    remainingSeconds: Int,
    onSetDuration: (Int) -> Unit,
    onStartContract: () -> Unit,
    onCancelContract: () -> Unit,
    onToggleAppBlock: (packageName: String, currentStatus: Boolean) -> Unit,
    onAddCustomApp: (String) -> Unit
) {
    var isAddAppDialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
    ) {
        if (isContractActive) {
            // ACTIVE LOCK OVERLAY (El Contrato en Ejecución)
            ActiveContractOverlay(
                remainingSeconds = remainingSeconds,
                totalMinutes = contractMinutes,
                onCancelContract = onCancelContract
            )
        } else {
            // CONTRACT SETUP & BLACKLIST CONFIGURATION
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, LupoFlameOrange, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = LupoFlameOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "El Contrato de Estudio ✍️",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LupoFlameOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Regulación conductual: Bloquea aplicaciones distractoras como TikTok, Instagram y Videojuegos durante tus sesiones de estudio para vencer la procrastinación.",
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Focus Duration Timer Picker
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Duración del Bloqueo de Enfoque",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LupoPrimaryGold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (mins in listOf(15, 25, 45, 60)) {
                                    val isSelected = mins == contractMinutes
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) LupoPrimaryGold else LupoCanvasDark)
                                            .border(1.dp, LupoCardBorder, RoundedCornerShape(12.dp))
                                            .clickable { onSetDuration(mins) }
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = "$mins min",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onStartContract,
                                colors = ButtonDefaults.buttonColors(containerColor = LupoFlameOrange),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("firmar_contrato_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Bloquear",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FIRMAR Y ACTIVAR CONTRATO (+$contractMinutes×10 EXP)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Lista Negra de Apps Distractoras
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lista Negra de Aplicaciones 📵",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoTextPrimary
                        )

                        OutlinedButton(
                            onClick = { isAddAppDialogOpen = true },
                            modifier = Modifier.testTag("add_custom_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir",
                                tint = LupoPrimaryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir App", fontSize = 11.sp, color = LupoPrimaryGold)
                        }
                    }
                }

                items(blacklistedApps) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LupoCardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (app.isBlocked) LupoFlameOrange.copy(alpha = 0.2f) else LupoCardBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            app.appName.contains("TikTok") -> "🎵"
                                            app.appName.contains("Instagram") -> "📸"
                                            app.appName.contains("YouTube") -> "▶️"
                                            app.appName.contains("Facebook") -> "📘"
                                            else -> "🎮"
                                        },
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = app.appName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (app.isBlocked) "Bloqueado en el Contrato" else "Permitido",
                                        fontSize = 11.sp,
                                        color = if (app.isBlocked) LupoFlameOrange else LupoEmeraldGreen
                                    )
                                }
                            }

                            Switch(
                                checked = app.isBlocked,
                                onCheckedChange = { onToggleAppBlock(app.packageName, app.isBlocked) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LupoFlameOrange,
                                    checkedTrackColor = LupoFlameOrange.copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = LupoCardBorder
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (isAddAppDialogOpen) {
        AddCustomAppModal(
            onDismiss = { isAddAppDialogOpen = false },
            onConfirm = { appName ->
                onAddCustomApp(appName)
                isAddAppDialogOpen = false
            }
        )
    }
}

@Composable
private fun ActiveContractOverlay(
    remainingSeconds: Int,
    totalMinutes: Int,
    onCancelContract: () -> Unit
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LupoFlameOrange.copy(alpha = 0.2f))
                .border(3.dp, LupoFlameOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🐺🔒", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "EL CONTRATO ESTÁ ACTIVO",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LupoFlameOrange,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Lupo está protegiendo tu atención. Las redes sociales y juegos están bloqueados.",
            fontSize = 12.sp,
            color = LupoTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Countdown Timer Text
        Text(
            text = timeFormatted,
            fontSize = 56.sp,
            fontWeight = FontWeight.Black,
            color = LupoPrimaryGold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¡Al terminar recibirás +${totalMinutes * 10} EXP y un Cofre de Recompensa!",
            fontSize = 13.sp,
            color = LupoEmeraldGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedButton(
            onClick = onCancelContract,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
            modifier = Modifier.testTag("cancel_contract_button")
        ) {
            Text("Rumpir Contrato (Perder Recompensas)", color = Color.Red, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AddCustomAppModal(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var appName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LupoSurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, LupoPrimaryGold, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Añadir Aplicación a Bloquear 📱",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LupoPrimaryGold
                )

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Nombre de la App (ej. Roblox, X, Snapchat)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_app_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (appName.isNotBlank()) {
                                onConfirm(appName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        modifier = Modifier.testTag("confirm_add_custom_app_button")
                    ) {
                        Text("Bloquear App", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
