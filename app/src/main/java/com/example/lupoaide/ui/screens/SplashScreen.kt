package com.example.lupoaide.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.R
import com.example.lupoaide.ui.theme.DarkPrimary
import com.example.lupoaide.ui.theme.LightPrimary
import com.example.lupoaide.ui.theme.TertiaryBoth
import com.example.lupoaide.ui.theme.DarkSecondary
import com.example.lupoaide.ui.theme.LightSecondary

@Composable
fun SplashScreen(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Si es modo claro: color primario del claro (#FFFFFF) y splash_light.jpg
    // Si es modo oscuro: color primario del oscuro (#000c3d) y splash_dark.jpg
    val backgroundColor = if (isDarkTheme) DarkPrimary else LightPrimary
    val imageRes = if (isDarkTheme) R.drawable.splash_dark else R.drawable.splash_light
    val titleColor = if (isDarkTheme) Color(0xFFFFFFFF) else DarkPrimary
    val subtitleColor = if (isDarkTheme) TertiaryBoth else Color(0xFF4F5058)
    val indicatorColor = if (isDarkTheme) TertiaryBoth else DarkPrimary
    val tipColor = if (isDarkTheme) LightSecondary else Color(0xFF4F5058)

    // Animación suave de pulso para la ilustración
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("lupo_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Ilustración central de Lupo con imagen según tema
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scaleAnim)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            if (isDarkTheme) Color(0xFF04144E) else Color(0xFFF1F5F9)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Pantalla de Carga Lupo Aide",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(36.dp))
                            .testTag(if (isDarkTheme) "splash_img_dark" else "splash_img_light")
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isDarkTheme) TertiaryBoth else DarkPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lupo Aide",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tu Compañero Inteligente de Estudio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                )
            }

            // Pie con indicador de carga y mensaje de motivación
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                CircularProgressIndicator(
                    color = indicatorColor,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("splash_progress_indicator")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isDarkTheme) {
                        "Modo Noche • Concentración profunda..."
                    } else {
                        "Modo Día • ¡Listo para conquistar tus metas!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = tipColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
