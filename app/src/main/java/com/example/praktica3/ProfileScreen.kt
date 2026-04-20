package com.example.praktica3

import android.Manifest
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Инициализация календаря и диалога выбора времени
    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val formatted = String.format("%02d:%02d", hour, minute)
            viewModel.onTimeChange(formatted)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted && state.isEditing) {
            viewModel.setEditing(false)
        }
    }

    LaunchedEffect(state.isEditing) {
        if (state.isEditing) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissions)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.handleImageSelection(context, uri) }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) viewModel.handleImageSelection(context, tempUri) }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    if (!state.isEditing) {
                        IconButton(onClick = { viewModel.setEditing(true) }) {
                            Icon(Icons.Default.Edit, null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Фото профиля
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable(enabled = state.isEditing) { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (state.photoUri != null) {
                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isEditing) {
                // Поле ФИО
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("ФИО") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = state.resumeUrl,
                    onValueChange = { viewModel.onResumeChange(it) },
                    label = { Text("URL резюме (PDF)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = state.pairTime,
                    onValueChange = { viewModel.onTimeChange(it) },
                    label = { Text("Время любимой пары") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.timeError != null,
                    trailingIcon = {
                        IconButton(onClick = { timePickerDialog.show() }) {
                            Icon(Icons.Outlined.Timer, contentDescription = "Выбрать время")
                        }
                    }
                )

                state.timeError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                Button(
                    onClick = {

                        viewModel.saveProfile(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.timeError == null && state.pairTime.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF39C12))
                ) {
                    Text("Сохранить")
                }
            } else {
                // Режим просмотра
                Text(state.name.ifEmpty { "Имя не указано" }, fontSize = 22.sp)

                if (state.pairTime.isNotEmpty()) {
                    Text("Любимая пара в: ${state.pairTime}", fontSize = 16.sp, color = Color.Gray)
                }

                if (state.resumeUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.downloadResume(context, state.resumeUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF39C12))
                    ) { Text("Скачать Резюме") }
                }
            }
        }
    }

    // Диалог выбора источника фото
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Выберите фото") },
            confirmButton = {
                TextButton(onClick = {
                    tempUri = viewModel.createPhotoUri(context)
                    cameraLauncher.launch(tempUri!!)
                    showDialog = false
                }) { Text("Камера") }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) { Text("Галерея") }
            }
        )
    }
}