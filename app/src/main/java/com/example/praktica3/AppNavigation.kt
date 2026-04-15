package com.example.praktica3

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun AppNavigation(
    viewModel: PlayerViewModel,
    profileViewModel: ProfileViewModel
) {
    val navController = rememberNavController()
    val showBadge by viewModel.showBadge.collectAsState()


    val navItemsList = listOf(
        Triple("home", Icons.Default.Home, "Главная"),
        Triple("list", Icons.Default.List, "Список"),
        Triple("video", Icons.Default.PlayArrow, "Видео"),
        Triple("profile", Icons.Default.Person, "Профиль"),
        Triple("notifications", Icons.Default.Info, "Инфо")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItemsList.forEach { (route, icon, label) ->
                    val isSelected = currentRoute == route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (route == "list" && showBadge) {
                                        Badge(
                                            containerColor = Color(0xFFF39C12),
                                            modifier = Modifier.size(8.dp)
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color(0xFFF39C12) else Color.Gray
                                )
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFFF39C12) else Color.Gray,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFEF5E7)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }

            composable("filters") {
                FilterScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("list") {
                val players by viewModel.filteredPlayers.collectAsState()

                PlayerListScreen(
                    players = players,
                    navController = navController,
                    viewModel = viewModel,
                    onPlayerClick = { id -> navController.navigate("details/$id") }
                )
            }

            composable("video") { VideoScreen(viewModel) }

            // Новый маршрут для экрана Профиля
            composable("profile") {
                ProfileScreen(viewModel = profileViewModel)
            }

            composable("notifications") { InfoScreen() }

            composable(
                "details/{playerId}",
                arguments = listOf(navArgument("playerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("playerId") ?: 0
                val player = viewModel.getPlayerById(id)
                player?.let {
                    PlayerDetailScreen(it, viewModel) { navController.popBackStack() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    players: List<Player>,
    navController: NavController,
    viewModel: PlayerViewModel,
    onPlayerClick: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Список игроков", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF39C12)),
            actions = {
                val showBadge by viewModel.showBadge.collectAsState()
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = { navController.navigate("filters") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = Color.White
                        )
                    }

                    if (showBadge) {
                        Surface(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp),
                            shape = CircleShape,
                            color = Color(0xFFF39C12),
                            border = BorderStroke(1.5.dp, Color.White)
                        ) {}
                    }
                }
            }
        )


        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(players) { player ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerClick(player.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = player.photoRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = player.name,
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        fontSize = 18.sp
                    )
                    Text(
                        text = player.number.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFF39C12)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(player: Player, viewModel: PlayerViewModel, onBack: () -> Unit) {
    val isFavorite by viewModel.isPlayerFavorite(player.id).collectAsState(initial = false)

    Column(Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Карточка игрока", color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
            actions = {
                IconButton(onClick = { viewModel.toggleFavorite(player, isFavorite) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF39C12))
        )
        ConstraintLayout(Modifier.fillMaxSize().padding(16.dp)) {
            val (photo, name, info, statsTitle, statsRow, field) = createRefs()
            Image(
                painter = painterResource(id = player.photoRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape).constrainAs(photo) {
                    top.linkTo(parent.top); start.linkTo(parent.start)
                },
                contentScale = ContentScale.Crop
            )
            Text(player.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(name) {
                top.linkTo(photo.top); bottom.linkTo(photo.bottom); start.linkTo(photo.end, 16.dp)
            })
            Column(Modifier.constrainAs(info) { top.linkTo(photo.bottom, 24.dp); start.linkTo(parent.start) }) {
                Text("ИНФОРМАЦИЯ", fontWeight = FontWeight.ExtraBold, color = Color.Gray, fontSize = 12.sp)
                Text("Возраст: ${player.age} лет", Modifier.padding(top = 4.dp))
                Text("Позиция: ${player.position}"); Text("Команда: ${player.team}")
            }
            Text("СТАТИСТИКА", fontWeight = FontWeight.ExtraBold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.constrainAs(statsTitle) {
                top.linkTo(info.bottom, 24.dp); start.linkTo(parent.start)
            })
            Row(Modifier.constrainAs(statsRow) { top.linkTo(statsTitle.bottom, 8.dp); start.linkTo(parent.start) }) {
                player.stats.forEach { (text, color) ->
                    Surface(color = Color(color), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 8.dp)) {
                        Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(180.dp).border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).background(Color(0xFFF9F9F9)).constrainAs(field) {
                top.linkTo(statsRow.bottom, 32.dp)
            }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF39C12))
                    Text("Схема позиции на поле", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Star, null, Modifier.size(100.dp), tint = Color(0xFFF39C12))
        Spacer(Modifier.height(16.dp))
        Text("ФК УРАЛ", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFF39C12))
        Text("Официальное приложение", fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun VideoScreen(viewModel: PlayerViewModel) {
    val state by viewModel.videoState.collectAsState()
    val context = LocalContext.current
    val rivals = listOf("Зенит", "Спартак", "ЦСКА", "Локомотив")

    LaunchedEffect(Unit) { viewModel.loadVideos() }

    Column(Modifier.fillMaxSize()) {
        Text("Видеогалерея матчей", modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)

        when (val s = state) {
            is VideoState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFFF39C12)) }
            is VideoState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = Color.Red) }
            is VideoState.Success -> {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(s.videos) { video ->
                        val index = s.videos.indexOf(video)
                        val rival = rivals.getOrElse(index % rivals.size) { "Соперник" }

                        Card(
                            modifier = Modifier.fillMaxWidth().height(200.dp).padding(bottom = 16.dp).clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=фк+урал+vs+$rival+обзор"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box {
                                Box(Modifier.fillMaxSize().background(Color.Black))
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    Modifier.size(64.dp).align(Alignment.Center),
                                    tint = Color(0xFFF39C12)
                                )
                                Surface(
                                    Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        "Обзор матча: Урал — $rival",
                                        Modifier.padding(12.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "ФК «Урал»", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFF39C12))
        Text(text = "Екатеринбург", fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        Text(text = "История", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Клуб основан в 1930 году и является одним из старейших в России.", fontSize = 16.sp, lineHeight = 24.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF5E7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Главные достижения:", fontWeight = FontWeight.Bold, color = Color(0xFFF39C12))
                Text("• Двукратный финалист Кубка России (2017, 2019)")
                Text("• Победитель Первенства ФНЛ (2013)")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fc-ural.ru"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF39C12)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("ОФИЦИАЛЬНЫЙ САЙТ", fontWeight = FontWeight.Bold)
        }
    }
}