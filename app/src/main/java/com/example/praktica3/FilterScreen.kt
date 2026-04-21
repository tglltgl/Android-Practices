package com.example.praktica3

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.praktica3.PlayerViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(viewModel: PlayerViewModel, onBack: () -> Unit) {
    val selectedFilter by viewModel.savedFilter.collectAsState()
    val onlyVeterans by viewModel.onlyVeterans.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {

            Text("Позиция игрока", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            val positions = listOf("Все", "Вратари", "Защитники", "Полузащитники", "Нападающие")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                positions.forEach { pos ->
                    FilterChip(
                        selected = selectedFilter == pos,
                        onClick = { viewModel.updateFilter(pos) },
                        label = { Text(pos) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Только ветераны", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Возраст 30+", color = Color.Gray, fontSize = 14.sp)
                }
                Switch(
                    checked = onlyVeterans,
                    onCheckedChange = { viewModel.updateVeteransFilter(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF39C12))
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF39C12))
            ) {
                Text("ГОТОВО", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}