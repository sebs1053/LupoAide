package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoPurpleAccent
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoTextPrimary

@Composable
fun BackpackModal(
    onDismiss: () -> Unit
) {
    val sampleNotes = listOf(
        "Biología Celular: La mitocondria genera ATP mediante la respiración celular.",
        "Álgebra: Fórmula cuadrática x = (-b ± √(b² - 4ac)) / (2a).",
        "Física I: Leyes de Newton - F = m * a (Fuerza = masa × aceleración).",
        "Taller de Redacción: Reglas de acentuación de palabras agudas, graves y esdrújulas."
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, LupoPurpleAccent, RoundedCornerShape(20.dp)),
            color = LupoCanvasDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = "Mochila de Estudio LupoAide",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_backpack")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Apuntes guardados y fichas de repaso rápidas:",
                    fontSize = 12.sp,
                    color = LupoCyanNode
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sampleNotes.size) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LupoCardBorder, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📌 Apunte #${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LupoPrimaryGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sampleNotes[index],
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
