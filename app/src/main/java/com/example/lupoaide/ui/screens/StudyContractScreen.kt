package com.example.lupoaide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lupoaide.data.local.StudyContractEntity

@Composable
fun StudyContractScreen(
    contracts: List<StudyContractEntity>,
    onAddContract: (title: String, goalMinutes: Int, targetDays: Int, penalty: String, rewardCoins: Int, rewardXp: Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.testTag("add_contract_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Contract")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Study Pacts & Contracts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Make commitments with Lupo. Stay accountable, earn major rewards!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(contracts) { contract ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contract.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "${contract.currentStreak}/${contract.targetDays} Days",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🎯 Goal: ${contract.goalMinutes} minutes of study per day",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "⚠️ Penalty if failed: ${contract.penaltyDescription}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Reward: +${contract.rewardCoins} Coins & +${contract.rewardXp} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var title by remember { mutableStateOf("") }
            var goalMins by remember { mutableStateOf("60") }
            var days by remember { mutableStateOf("5") }
            var penalty by remember { mutableStateOf("No gaming for 2 hours") }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Sign Study Pact") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Pact Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = goalMins,
                            onValueChange = { goalMins = it },
                            label = { Text("Daily Study Minutes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = days,
                            onValueChange = { days = it },
                            label = { Text("Target Days Streak") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = penalty,
                            onValueChange = { penalty = it },
                            label = { Text("Penalty Commitment") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddContract(
                                    title,
                                    goalMins.toIntOrNull() ?: 60,
                                    days.toIntOrNull() ?: 5,
                                    penalty,
                                    50,
                                    100
                                )
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Sign Pact")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
