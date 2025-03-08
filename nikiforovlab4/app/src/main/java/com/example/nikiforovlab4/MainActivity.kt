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
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(false) } // Состояние темы
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Menu", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                    Divider()
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("done")
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("News")
                    }
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("tab2")
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Schedule")
                    }
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("extra")
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("History Archive")
                    }
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("tab1")
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Latest Transfers")
                    }
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings")
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Settings")
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My App") },
                        modifier = Modifier.fillMaxWidth(),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("main") }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainContent(navController, paddingValues) }
                    composable("done") { DoneScreen(paddingValues) }
                    composable("tab2") { TabScreen2(paddingValues) }
                    composable("extra") { ExtraScreen(paddingValues) }
                    composable("tab1") { TabScreen1(paddingValues) }
                }
            }
        }
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

@Composable
fun DoneScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("News", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        // 5 длинных кнопок
        Button(
            onClick = { /* Действие 1 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("Lorem ipsum: dolor sit amet, consectetur adipiscing elit.", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 2 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("A very important article", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 3 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("Not so important article", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 4 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("Clickbait article", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 5 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("An unimportant article", fontSize = 16.sp)
        }
    }
}

@Composable
fun TabScreen2(paddingValues: PaddingValues) {
    val buttonStates = remember { mutableStateListOf(*List(9) { false }.toTypedArray()) }
    var selectedTab by remember { mutableStateOf(0) }
    val buttonLabels = listOf(
        "Soccer", "Football", "Hockey",
        "Baseball", "Basketball", "Polo",
        "F-1", "Curling", "Volleyball"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // 9 круглых кнопок
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
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = buttonLabels[index],
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3 вертикальные вкладки с уникальными горизонтальными элементами
        items(3) { i ->
            Tab(
                selected = selectedTab == i,
                onClick = { selectedTab = i },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    when (i) {
                        0 -> {
                            Text("Team 1", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Time", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Team 2", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        1 -> {
                            Text("Team 1", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Time", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Team 2", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        2 -> {
                            Text("Team 1", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Time", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Team 2", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExtraScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("History Archive", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        Button(
            onClick = { /* Действие 1 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("The Star and Death of Diego Armandos", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 2 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("How VAR changed the world", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 3 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("\"MoneyBall\": History of Billy Beane's innovative tactic", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 4 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("Tottenham: Was \"In Bruges\" right?", fontSize = 16.sp)
        }
        Button(
            onClick = { /* Действие 5 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("History of World Cups", fontSize = 16.sp)
        }
    }
}

@Composable
fun TabScreen1(paddingValues: PaddingValues) {
    val buttonStates = remember { mutableStateListOf(*List(9) { false }.toTypedArray()) }
    var selectedTab by remember { mutableStateOf(0) }
    val buttonLabels = listOf(
        "Soccer", "Football", "Hockey",
        "Baseball", "Basketball", "Polo",
        "F-1", "Curling", "Volleyball"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // 9 круглых кнопок
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
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = buttonLabels[index],
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3 вертикальные вкладки с уникальными горизонтальными элементами
        items(3) { i ->
            Tab(
                selected = selectedTab == i,
                onClick = { selectedTab = i },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    when (i) {
                        0 -> {
                            Text("Player", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Price", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Where to", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        1 -> {
                            Text("Player", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Price", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Where to", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        2 -> {
                            Text("Player", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Price", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Where to", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenPreview() {
    MainScreen()
}