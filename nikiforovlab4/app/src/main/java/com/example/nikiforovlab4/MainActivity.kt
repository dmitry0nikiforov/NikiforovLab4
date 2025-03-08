package com.example.nikiforovlab4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}


@Composable
fun MainContent(navController: androidx.navigation.NavController, paddingValues: PaddingValues) {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    val buttonStates = remember { mutableStateListOf(*List(9) { false }.toTypedArray()) }
    val buttonLabels = listOf(
        "Soccer", "Football", "Hockey",
        "Baseball", "Basketball", "Polo",
        "F-1", "Curling", "Volleyball"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = text1,
                onValueChange = { text1 = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = text2,
                onValueChange = { text2 = it },
                label = { Text("Surname") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isSelected = buttonStates[index]

                        Button(
                            onClick = { buttonStates[index] = !buttonStates[index] },
                            modifier = Modifier
                                .padding(4.dp)
                                .size(115.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = buttonLabels[index],
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.navigate("done") },
            modifier = Modifier
                .padding(top = 16.dp)
                .width(200.dp)
        ) {
            Text(text = "Done", fontSize = 18.sp)
        }
    }
}