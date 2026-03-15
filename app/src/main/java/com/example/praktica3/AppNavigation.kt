package com.example.praktica3

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun AppNavigation(viewModel: PlayerViewModel) {
    val navController = rememberNavController()

    val items = listOf(
        Triple("home", Icons.Default.Home, "Главная"),
        Triple("list", Icons.Default.List, "Список"),
        Triple("video", Icons.Default.PlayArrow, "Видео"),
        Triple("notifications", Icons.Default.Info, "Инфо")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
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
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (currentRoute == route) Color(0xFFF39C12) else Color.Gray
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFFEF5E7))
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
            composable("list") {
                val players by viewModel.players.collectAsState()
                PlayerListScreen(players) { id -> navController.navigate("details/$id") }
            }
            composable("video") { VideoScreen() }
            composable("notifications") { InfoScreen() }

            composable(
                "details/{playerId}",
                arguments = listOf(navArgument("playerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("playerId") ?: 0
                val player = viewModel.getPlayerById(id)
                player?.let { PlayerDetailScreen(it) { navController.popBackStack() } }
            }
        }
    }
}


@Composable
fun HomeScreen() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Star, null, Modifier.size(100.dp), tint = Color(0xFFF39C12))
        Spacer(Modifier.height(16.dp))
        Text("ФК УРАЛ", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFF39C12))
        Text("Официальное приложение", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Text("Ближайший матч: Урал - Зенит\n18 марта, 19:00", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VideoScreen() {
    Column(Modifier.fillMaxSize()) {
        Text("Видеогалерея", Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(5) { index ->
                Card(
                    Modifier.fillMaxWidth().height(180.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(Modifier.background(Color.DarkGray).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(60.dp), tint = Color.White)
                        Text("Обзор матча #$index", Modifier.align(Alignment.BottomStart).padding(12.dp), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("О клубе", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "«Урал» — российский профессиональный футбольный клуб из Екатеринбурга. " +
                    "Основан в 1930 году. Один из старейших клубов России.",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        Text("Контакты", fontWeight = FontWeight.Bold)
        Text("Стадион: Екатеринбург Арена")
        Text("Сайт: fc-ural.ru")
        Spacer(Modifier.height(32.dp))
        Text("Версия приложения: 1.0.4", color = Color.LightGray, fontSize = 12.sp)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(players: List<Player>, onPlayerClick: (Int) -> Unit) {
    Column {
        TopAppBar(
            title = { Text("Список игроков", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF39C12)),
            actions = { IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = Color.White) } }
        )
        LazyColumn {
            items(players) { player ->
                Row(
                    Modifier.fillMaxWidth().clickable { onPlayerClick(player.id) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(id = player.photoRes),
                        contentDescription = "Фото ${player.name}",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Text(player.name, Modifier.weight(1f).padding(start = 16.dp), fontSize = 18.sp)
                    Text(player.number.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF39C12))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(player: Player, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Карточка игрока", color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF39C12))
        )
        ConstraintLayout(Modifier.fillMaxSize().padding(16.dp)) {
            val (photo, name, info, statsTitle, statsRow, field) = createRefs()


            Image(
                painter = painterResource(id = player.photoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .constrainAs(photo) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    },
                contentScale = ContentScale.Crop
            )

            Text(player.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.constrainAs(name) {
                top.linkTo(photo.top); bottom.linkTo(photo.bottom); start.linkTo(photo.end, 16.dp)
            })

            Column(Modifier.constrainAs(info) { top.linkTo(photo.bottom, 24.dp); start.linkTo(parent.start) }) {
                Text("ИНФОРМАЦИЯ", fontWeight = FontWeight.ExtraBold, color = Color.Gray, fontSize = 12.sp)
                Text("Возраст: ${player.age} лет", Modifier.padding(top = 4.dp))
                Text("Позиция: ${player.position}")
                Text("Команда: ${player.team}")
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