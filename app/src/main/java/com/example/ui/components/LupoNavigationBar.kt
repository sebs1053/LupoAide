package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoNavBg
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoTextSecondary

@Composable
fun LupoNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(LupoNavBg)
            .border(1.dp, LupoCardBorder)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = "🏠",
            label = "Inicio",
            isSelected = selectedTab == 0,
            testTag = "nav_home",
            onClick = { onTabSelected(0) }
        )
        NavItem(
            icon = "📅",
            label = "Horario",
            isSelected = selectedTab == 1,
            testTag = "nav_schedule",
            onClick = { onTabSelected(1) }
        )
        NavItem(
            icon = "🏋️",
            label = "Contrato",
            isSelected = selectedTab == 2,
            testTag = "nav_contract",
            onClick = { onTabSelected(2) }
        )
        NavItem(
            icon = "✔️",
            label = "Tareas",
            isSelected = selectedTab == 3,
            testTag = "nav_tasks",
            onClick = { onTabSelected(3) }
        )
        NavItem(
            icon = "👤",
            label = "Perfil",
            isSelected = selectedTab == 4,
            testTag = "nav_profile",
            onClick = { onTabSelected(4) }
        )
    }
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val activeColor = LupoPrimaryGold
    val inactiveColor = LupoTextSecondary

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) activeColor else inactiveColor
            )
        }
    }
}
