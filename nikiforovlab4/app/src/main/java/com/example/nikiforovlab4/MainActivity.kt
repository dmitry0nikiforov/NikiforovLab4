package com.example.lab4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.io.File
import java.io.IOException

// Кастомный Result для обработки результатов
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
}

// Модели данных
data class ButtonState(val label: String, val isSelected: Boolean = false)

data class MainScreenState(
    val text1: String = "",
    val text2: String = "",
    val buttons: List<ButtonState> = listOf(
        ButtonState("Soccer"), ButtonState("Football"), ButtonState("Hockey"),
        ButtonState("Baseball"), ButtonState("Basketball"), ButtonState("Polo"),
        ButtonState("F-1"), ButtonState("Curling"), ButtonState("Volleyball")
    ),
    val allLeagues: List<LeagueItem> = emptyList(),
    val filteredLeagues: List<LeagueItem> = emptyList(),
    val teams: List<TeamItem>? = null,
    val isLoadingLeagues: Boolean = false,
    val isLoadingTeams: Boolean = false,
    val error: String? = null
)

data class SettingsScreenState(
    val isDarkTheme: Boolean = false,
    val isRussian: Boolean = false
)

data class TransfersScreenState(
    val buttonStates: List<Boolean> = List(9) { false },
    val isLoading: Boolean = false,
    val error: String? = null
)

data class LeaguesResponse(val response: List<LeagueItem>)
data class LeagueItem(val league: League, val country: Country)
data class League(val id: Int, val name: String, val type: String, val logo: String)
data class Country(val name: String, val code: String)
data class TeamsResponse(val response: List<TeamItem>)
data class TeamItem(val team: Team)
data class Team(val id: Int, val name: String, val logo: String)

// Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://v3.football.api-sports.io/"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("leagues")
    fun getLeagues(@Header("x-apisports-key") apiKey: String): Call<LeaguesResponse>

    @GET("teams")
    fun getTeams(@Header("x-apisports-key") apiKey: String, @Query("league") leagueId: Int): Call<TeamsResponse>
}

// Репозиторий для экрана Transfers с использованием TXT
class TransfersRepository(private val context: Context? = null) {
    private val file = context?.filesDir?.let { File(it, "transfers_state.txt") }

    suspend fun loadButtonStates(): Result<List<Boolean>> {
        return if (context == null || file == null) {
            // Для превью или случаев, когда контекст недоступен
            Result.Success(List(9) { false })
        } else {
            try {
                if (file.exists()) {
                    val buttonsString = file.readText()
                    val buttonStates = buttonsString.split(",").map { it.toBoolean() }
                    if (buttonStates.size == 9) {
                        Result.Success(buttonStates)
                    } else {
                        Result.Failure(IllegalStateException("Некорректный формат данных в файле"))
                    }
                } else {
                    Result.Success(List(9) { false }) // Значения по умолчанию, если файла нет
                }
            } catch (e: IOException) {
                Result.Failure(e)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }
    }

    suspend fun saveButtonStates(buttonStates: List<Boolean>): Result<Unit> {
        return if (context == null || file == null) {
            // Для превью ничего не сохраняем
            Result.Success(Unit)
        } else {
            try {
                val buttonsString = buttonStates.joinToString(",")
                file.writeText(buttonsString)
                Result.Success(Unit)
            } catch (e: IOException) {
                Result.Failure(e)
            }
        }
    }
}

// ViewModel для экрана Transfers
class TransfersViewModel(private val repository: TransfersRepository) : ViewModel() {
    private val _state = MutableStateFlow(TransfersScreenState())
    val state: StateFlow<TransfersScreenState> = _state.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = repository.loadButtonStates()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        buttonStates = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Failure -> {
                    _state.value = _state.value.copy(
                        buttonStates = List(9) { false }, // Значения по умолчанию при ошибке
                        isLoading = false,
                        error = "Ошибка загрузки данных: ${result.exception.message}"
                    )
                }
            }
        }
    }

    fun toggleButton(index: Int) {
        val updatedButtons = _state.value.buttonStates.toMutableList().apply {
            this[index] = !this[index]
        }
        _state.value = _state.value.copy(buttonStates = updatedButtons)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.saveButtonStates(updatedButtons)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(error = null)
                }
                is Result.Failure -> {
                    _state.value = _state.value.copy(error = "Ошибка сохранения: ${result.exception.message}")
                }
            }
        }
    }
}

// Другие ViewModel (без хранения)
class SportsRepository {
    private val apiKey = "65d3130ac3b6be680768a1d5802ed7b2"
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    fun fetchLeagues() {
        _state.value = _state.value.copy(isLoadingLeagues = true, error = null)
        RetrofitClient.apiService.getLeagues(apiKey).enqueue(object : Callback<LeaguesResponse> {
            override fun onResponse(call: Call<LeaguesResponse>, response: Response<LeaguesResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.response.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            allLeagues = body.response,
                            isLoadingLeagues = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            allLeagues = emptyList(),
                            isLoadingLeagues = false,
                            error = "No leagues found"
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        allLeagues = emptyList(),
                        isLoadingLeagues = false,
                        error = "Server error: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<LeaguesResponse>, t: Throwable) {
                _state.value = _state.value.copy(
                    allLeagues = emptyList(),
                    isLoadingLeagues = false,
                    error = "Network error: ${t.message}"
                )
            }
        })
    }

    fun fetchTeams(leagueId: Int) {
        _state.value = _state.value.copy(isLoadingTeams = true, error = null)
        RetrofitClient.apiService.getTeams(apiKey, leagueId).enqueue(object : Callback<TeamsResponse> {
            override fun onResponse(call: Call<TeamsResponse>, response: Response<TeamsResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.response.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            teams = body.response,
                            isLoadingTeams = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            teams = null,
                            isLoadingTeams = false,
                            error = "No teams found"
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        teams = null,
                        isLoadingTeams = false,
                        error = "Server error: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<TeamsResponse>, t: Throwable) {
                _state.value = _state.value.copy(
                    teams = null,
                    isLoadingTeams = false,
                    error = "Network error: ${t.message}"
                )
            }
        })
    }
}

class MainViewModel : ViewModel() {
    private val repository = SportsRepository()
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        repository.fetchLeagues()
        viewModelScope.launch {
            repository.state.collect { repoState ->
                _state.value = repoState.copy(filteredLeagues = filterLeagues(repoState))
            }
        }
    }

    private fun filterLeagues(repoState: MainScreenState): List<LeagueItem> {
        val selectedSports = repoState.buttons
            .filter { it.isSelected }
            .map { it.label.lowercase() }

        return if (selectedSports.isEmpty()) {
            repoState.allLeagues
        } else {
            repoState.allLeagues.filter { league ->
                val leagueName = league.league.name.lowercase()
                selectedSports.any { sport ->
                    leagueName.contains(sport) || sport == "soccer" && leagueName.contains("football")
                }
            }
        }
    }

    fun fetchTeams(leagueId: Int) {
        repository.fetchTeams(leagueId)
    }

    fun updateText1(newText: String) {
        _state.value = _state.value.copy(text1 = newText)
    }

    fun updateText2(newText: String) {
        _state.value = _state.value.copy(text2 = newText)
    }

    fun toggleButton(index: Int) {
        val updatedButtons = _state.value.buttons.mapIndexed { i, button ->
            if (i == index) button.copy(isSelected = !button.isSelected) else button
        }
        _state.value = _state.value.copy(buttons = updatedButtons)
    }
}

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    fun toggleDarkTheme(isEnabled: Boolean) {
        _state.value = _state.value.copy(isDarkTheme = isEnabled)
    }

    fun toggleRussianLanguage(isEnabled: Boolean) {
        _state.value = _state.value.copy(isRussian = isEnabled)
    }
}

// Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel = MainViewModel()
            val settingsViewModel = SettingsViewModel()
            val transfersViewModel = TransfersViewModel(TransfersRepository(this))
            MainScreen(mainViewModel, settingsViewModel, transfersViewModel)
        }
    }
}

// UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    transfersViewModel: TransfersViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val settingsState by settingsViewModel.state.collectAsState()

    MaterialTheme(
        colorScheme = if (settingsState.isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Menu", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                    Divider()
                    Button(
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("done") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) { Text("News") }
                    Button(
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("tab2") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) { Text("Schedule") }
                    Button(
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("extra") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) { Text("History Archive") }
                    Button(
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("tab1") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) { Text("Latest Transfers") }
                    Button(
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("settings") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) { Text("Settings") }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My App") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("main") }) {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainContent(mainViewModel, navController, paddingValues) }
                    composable("done") { DoneScreen(paddingValues) }
                    composable("extra") { ExtraScreen(paddingValues) }
                    composable("tab1") { TabScreen1(paddingValues, transfersViewModel) }
                    composable("tab2") { TabScreen2(paddingValues) }
                    composable("settings") { SettingsScreen(settingsViewModel, paddingValues) }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: MainViewModel,
    navController: androidx.navigation.NavController,
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

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
                value = state.text1,
                onValueChange = { viewModel.updateText1(it) },
                label = { Text("Field 1") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state.text2,
                onValueChange = { viewModel.updateText2(it) },
                label = { Text("Field 2") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val button = state.buttons[index]

                        Button(
                            onClick = { viewModel.toggleButton(index) },
                            modifier = Modifier.padding(4.dp).size(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (button.isSelected) Color.Green else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = button.label,
                                fontSize = 14.sp,
                                color = if (button.isSelected) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }

            when {
                state.isLoadingLeagues -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                state.error != null -> Text(
                    text = state.error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
                state.filteredLeagues.isNotEmpty() -> {
                    LazyColumn {
                        items(state.filteredLeagues) { league ->
                            Text(
                                text = "${league.league.name} (${league.country.name})",
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { scope.launch { viewModel.fetchTeams(league.league.id) } }
                            )
                        }
                    }
                }
                else -> Text(text = "No leagues match selected sports", modifier = Modifier.padding(16.dp))
            }

            state.teams?.let { teams ->
                when {
                    state.isLoadingTeams -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    state.error != null -> Text(
                        text = state.error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                    teams.isNotEmpty() -> {
                        LazyColumn {
                            items(teams) { teamItem ->
                                Text(
                                    text = teamItem.team.name,
                                    fontSize = 16.sp,
                                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.navigate("done") },
            modifier = Modifier.padding(top = 16.dp).width(200.dp)
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
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("Lorem ipsum: dolor sit amet, consectetur adipiscing elit.", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("A very important article", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("Not so important article", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("Clickbait article", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("An unimportant article", fontSize = 16.sp) }
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
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("The Star and Death of Diego Armandos", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("How VAR changed the world", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("\"MoneyBall\": History of Billy Beane's innovative tactic", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(50.dp)
        ) { Text("Tottenham: Was \"In Bruges\" right?", fontSize = 16.sp) }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(50.dp)
        ) { Text("History of World Cups", fontSize = 16.sp) }
    }
}

@Composable
fun TabScreen1(paddingValues: PaddingValues, viewModel: TransfersViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val isSelected = state.buttonStates[index]

                                Button(
                                    onClick = { viewModel.toggleButton(index) },
                                    modifier = Modifier.padding(4.dp).size(60.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = buttonLabels[index],
                                        fontSize = 16.sp,
                                        color = if (isSelected) Color.White else Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                            Text("Player", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Price", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Where to", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TabScreen2(paddingValues: PaddingValues) {
    val buttonStates = remember { mutableStateListOf(*List(9) { false }.toTypedArray()) }
    var selectedTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
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
                            modifier = Modifier.padding(4.dp).size(60.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                fontSize = 16.sp,
                                color = if (isSelected) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    Text("Team 1", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("Time", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("Team 2", fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, paddingValues: PaddingValues) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if (state.isRussian) "Настройки" else "Settings",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isRussian) "Тёмная тема" else "Dark Theme",
                fontSize = 18.sp
            )
            Switch(
                checked = state.isDarkTheme,
                onCheckedChange = { viewModel.toggleDarkTheme(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isRussian) "Русский язык" else "Russian Language",
                fontSize = 18.sp
            )
            Switch(
                checked = state.isRussian,
                onCheckedChange = { viewModel.toggleRussianLanguage(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenPreview() {
    // Для превью используем TransfersRepository без контекста
    MainScreen(
        mainViewModel = MainViewModel(),
        settingsViewModel = SettingsViewModel(),
        transfersViewModel = TransfersViewModel(TransfersRepository())
    )
}