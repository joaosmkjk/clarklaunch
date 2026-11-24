package com.clarklaunch.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Canvas = Color(0xFFF8F9FB)
private val White = Color(0xFFFFFFFF)
private val Ink = Color(0xFF282B32)
private val SoftInk = Color(0xFF777D88)
private val Rose = Color(0xFFB85E7A)
private val RoseWash = Color(0xFFF7EAF0)
private val Blue = Color(0xFF6FA9CE)
private val BlueWash = Color(0xFFEAF4FA)
private val Line = Color(0xFFE9EDF1)

private data class LaunchableApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap
)

class MainActivity : ComponentActivity() {
    private var installedApps by mutableStateOf<List<LaunchableApp>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = android.graphics.Color.rgb(248, 249, 251)
        window.navigationBarColor = android.graphics.Color.rgb(248, 249, 251)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Rose,
                    secondary = Blue,
                    background = Canvas,
                    surface = White,
                    onPrimary = White,
                    onBackground = Ink,
                    onSurface = Ink
                )
            ) {
                ClarkLaunchHome(
                    apps = installedApps,
                    initialFavorites = getSavedFavorites(),
                    initialWallpaper = getSavedWallpaper(),
                    initialWallpaperOpacity = getSavedWallpaperOpacity(),
                    initialLockedApps = getSavedLockedApps(),
                    initialHasPin = AppLockSecurity.hasPin(this@MainActivity),
                    onSetPin = { AppLockSecurity.setPin(this@MainActivity, it) },
                    onLockedAppsChanged = ::saveLockedApps,
                    onLaunchApp = ::launchApp,
                    onFavoritesChanged = ::saveFavorites,
                    onWallpaperChanged = ::saveWallpaper,
                    onWallpaperOpacityChanged = ::saveWallpaperOpacity
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        installedApps = loadLaunchableApps()
    }

    private fun loadLaunchableApps(): List<LaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val appPackage = activityInfo.applicationInfo.packageName
                if (appPackage == packageName) return@mapNotNull null
                val icon = try {
                    packageManager.getApplicationIcon(appPackage).toBitmap(96, 96)
                } catch (_: Exception) {
                    return@mapNotNull null
                }
                LaunchableApp(
                    label = info.loadLabel(packageManager).toString(),
                    packageName = appPackage,
                    icon = icon
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        startActivity(intent)
    }

    private fun getSavedFavorites(): Set<String> =
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .getStringSet("favorites", emptySet())?.toSet() ?: emptySet()

    private fun saveFavorites(favorites: Set<String>) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putStringSet("favorites", favorites).apply()
    }

    private fun getSavedWallpaperOpacity(): Float =
        getSharedPreferences("clarklaunch", MODE_PRIVATE).getFloat("wallpaper_opacity", 0.34f)

    private fun saveWallpaperOpacity(value: Float) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putFloat("wallpaper_opacity", value).apply()
    }

    private fun getSavedWallpaper(): String? =
        getSharedPreferences("clarklaunch", MODE_PRIVATE).getString("wallpaper_uri", null)

    private fun saveWallpaper(uri: String) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putString("wallpaper_uri", uri).apply()
    }

    private fun getSavedLockedApps(): Set<String> =
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .getStringSet("locked_apps", emptySet())?.toSet() ?: emptySet()

    private fun saveLockedApps(packages: Set<String>) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putStringSet("locked_apps", packages).apply()
    }
}

@Composable
private fun ClarkLaunchHome(
    apps: List<LaunchableApp>,
    initialFavorites: Set<String>,
    initialWallpaper: String?,
    initialWallpaperOpacity: Float,
    initialLockedApps: Set<String>,
    initialHasPin: Boolean,
    onSetPin: (String) -> Unit,
    onLockedAppsChanged: (Set<String>) -> Unit,
    onLaunchApp: (String) -> Unit,
    onFavoritesChanged: (Set<String>) -> Unit,
    onWallpaperChanged: (String) -> Unit,
    onWallpaperOpacityChanged: (Float) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var showAllApps by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(initialFavorites) }
    var wallpaperUri by remember { mutableStateOf(initialWallpaper?.let(Uri::parse)) }
    var wallpaperOpacity by remember { mutableStateOf(initialWallpaperOpacity) }
    var showSettings by remember { mutableStateOf(false) }
    var favoriteLimitMessage by remember { mutableStateOf(false) }
    var lockedApps by remember { mutableStateOf(initialLockedApps) }
    var hasPin by remember { mutableStateOf(initialHasPin) }
    var showLockManager by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }
    var pendingLockedPackage by remember { mutableStateOf<String?>(null) }
    var unlockPin by remember { mutableStateOf("") }
    var unlockError by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(30_000)
        }
    }

    val timeText = currentTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    val dateText = currentTime.format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
    ).replaceFirstChar { it.uppercase(Locale("pt", "BR")) }

    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Provider did not grant persistable access; retain the selected URI for this session.
            }
            wallpaperUri = uri
            onWallpaperChanged(uri.toString())
        }
    }

    val wallpaperBitmap = remember(wallpaperUri) {
        wallpaperUri?.let { uri ->
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) {
                    sample *= 2
                }
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    val filteredApps = apps.filter { it.label.contains(search, ignoreCase = true) }
    val favoriteApps = apps.filter { it.packageName in favorites }.take(5)

    BackHandler(enabled = showAllApps || search.isNotEmpty()) {
        if (showAllApps) showAllApps = false else search = ""
    }

    if (showPinSetup) {
        AlertDialog(
            onDismissRequest = { showPinSetup = false; newPin = ""; confirmPin = ""; pinError = "" },
            title = { Text("Criar PIN de proteção", color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Crie um PIN de 4 a 8 dígitos. Ele será usado para abrir os aplicativos protegidos dentro do ClarkLaunch.", color = SoftInk, fontSize = 13.sp)
                    BasicTextField(value = newPin, onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) newPin = it }, singleLine = true, visualTransformation = PasswordVisualTransformation(), textStyle = MaterialTheme.typography.titleLarge.copy(color = Ink), decorationBox = { inner -> Box { if (newPin.isEmpty()) Text("Novo PIN", color = SoftInk); inner() } })
                    BasicTextField(value = confirmPin, onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) confirmPin = it }, singleLine = true, visualTransformation = PasswordVisualTransformation(), textStyle = MaterialTheme.typography.titleLarge.copy(color = Ink), decorationBox = { inner -> Box { if (confirmPin.isEmpty()) Text("Confirmar PIN", color = SoftInk); inner() } })
                    if (pinError.isNotEmpty()) Text(pinError, color = Rose, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        newPin.length !in 4..8 -> pinError = "Use entre 4 e 8 dígitos."
                        newPin != confirmPin -> pinError = "Os PINs não correspondem."
                        else -> { onSetPin(newPin); hasPin = true; showPinSetup = false; showLockManager = true; newPin = ""; confirmPin = ""; pinError = "" }
                    }
                }) { Text("Salvar PIN", color = Blue) }
            },
            dismissButton = { TextButton(onClick = { showPinSetup = false }) { Text("Cancelar", color = SoftInk) } },
            containerColor = White
        )
    }

    if (showLockManager) {
        AlertDialog(
            onDismissRequest = { showLockManager = false },
            title = { Text("Aplicativos protegidos", color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (hasPin) "Marque os aplicativos que pedirão seu PIN ao serem abertos pelo ClarkLaunch." else "Configure um PIN antes de escolher os aplicativos protegidos.", color = SoftInk, fontSize = 12.sp)
                    if (hasPin) {
                        LazyColumn(modifier = Modifier.height(330.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(apps, key = { it.packageName }) { app ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Image(bitmap = app.icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(28.dp))
                                    Text(app.label, color = Ink, fontSize = 13.sp, modifier = Modifier.weight(1f).padding(start = 9.dp))
                                    Checkbox(checked = app.packageName in lockedApps, onCheckedChange = { checked ->
                                        lockedApps = if (checked) lockedApps + app.packageName else lockedApps - app.packageName
                                        onLockedAppsChanged(lockedApps)
                                    })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (hasPin) showLockManager = false else { showLockManager = false; showPinSetup = true } }) {
                    Text(if (hasPin) "Concluir" else "Criar PIN", color = Blue)
                }
            },
            dismissButton = { TextButton(onClick = { showLockManager = false }) { Text("Fechar", color = SoftInk) } },
            containerColor = White
        )
    }

    if (pendingLockedPackage != null) {
        AlertDialog(
            onDismissRequest = { pendingLockedPackage = null; unlockPin = ""; unlockError = "" },
            title = { Text("Aplicativo protegido", color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Digite seu PIN para continuar.", color = SoftInk, fontSize = 13.sp)
                    BasicTextField(value = unlockPin, onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) unlockPin = it }, singleLine = true, visualTransformation = PasswordVisualTransformation(), textStyle = MaterialTheme.typography.titleLarge.copy(color = Ink), decorationBox = { inner -> Box { if (unlockPin.isEmpty()) Text("PIN", color = SoftInk); inner() } })
                    if (unlockError.isNotEmpty()) Text(unlockError, color = Rose, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (AppLockSecurity.verify(context, unlockPin)) {
                        val packageToLaunch = pendingLockedPackage
                        pendingLockedPackage = null
                        unlockPin = ""
                        unlockError = ""
                        if (packageToLaunch != null) onLaunchApp(packageToLaunch)
                    } else unlockError = "PIN incorreto. Tente novamente."
                }) { Text("Desbloquear", color = Blue) }
            },
            dismissButton = { TextButton(onClick = { pendingLockedPackage = null; unlockPin = ""; unlockError = "" }) { Text("Cancelar", color = SoftInk) } },
            containerColor = White
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Ajustes do ClarkLaunch", color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Intensidade do papel de parede", color = Ink, fontWeight = FontWeight.Medium)
                    Slider(
                        value = wallpaperOpacity,
                        onValueChange = {
                            wallpaperOpacity = it
                            onWallpaperOpacityChanged(it)
                        },
                        valueRange = 0.12f..0.60f
                    )
                    Text(
                        "Ajuste entre um fundo discreto e uma imagem mais presente.",
                        color = SoftInk,
                        fontSize = 12.sp
                    )
                    Text(
                        "Dock: até 5 aplicativos favoritos.",
                        color = Blue,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Concluir", color = Blue)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    wallpaperUri = null
                    onWallpaperChanged("")
                    showSettings = false
                }) {
                    Text("Remover wallpaper", color = Rose)
                }
            },
            containerColor = White
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
        var horizontalDrag by remember { mutableStateOf(0f) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(showAllApps) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            horizontalDrag += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            if (horizontalDrag < -90f) showAllApps = true
                            if (horizontalDrag > 90f) showAllApps = false
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
                    )
                }
        ) {
            if (wallpaperBitmap != null) {
                Image(
                    bitmap = wallpaperBitmap,
                    contentDescription = "Papel de parede",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(wallpaperOpacity)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Canvas.copy(alpha = 1f - wallpaperOpacity * 0.42f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 25.dp, vertical = 20.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "C L A R K",
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.8.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("a quieter kind of home", color = SoftInk, fontSize = 10.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BlueWash, CircleShape)
                                .clickable { showLockManager = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = "Bloqueio de aplicativos", tint = Blue)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BlueWash, CircleShape)
                                .clickable { wallpaperPicker.launch(arrayOf("image/*")) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = "Escolher wallpaper", tint = Blue)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RoseWash, CircleShape)
                                .clickable { showSettings = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Configurações", tint = Rose)
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(timeText, color = Ink, fontSize = 58.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Text(dateText, color = SoftInk, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))

                Text("Leveza em", color = Ink, fontSize = 31.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
                Text("cada detalhe.", color = Rose, fontSize = 31.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
                Spacer(Modifier.height(8.dp))
                Text("Seu mundo, em harmonia.", color = SoftInk, fontSize = 13.sp)

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White.copy(alpha = 0.94f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = Blue)
                    Spacer(Modifier.size(11.dp))
                    BasicTextField(
                        value = search,
                        onValueChange = {
                            search = it
                            if (it.isNotBlank()) showAllApps = true
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        decorationBox = { innerTextField ->
                            Box {
                                if (search.isEmpty()) Text("Buscar um aplicativo", color = SoftInk, fontSize = 13.sp)
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showAllApps) "BIBLIOTECA" else "SEUS FAVORITOS",
                        color = SoftInk,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (showAllApps) "VOLTAR" else "VER TODOS",
                        color = Blue,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable {
                            showAllApps = !showAllApps
                            if (!showAllApps) search = ""
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (!showAllApps) {
                    if (favoriteApps.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White.copy(alpha = 0.94f), RoundedCornerShape(20.dp))
                                .clickable { showAllApps = true }
                                .padding(18.dp)
                        ) {
                            Text("Sua dock começa aqui", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(5.dp))
                            Text("Abra a biblioteca e toque na estrela para fixar até cinco favoritos.", color = SoftInk, fontSize = 12.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White.copy(alpha = 0.94f), RoundedCornerShape(22.dp))
                                .padding(vertical = 15.dp, horizontal = 7.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            favoriteApps.forEach { app ->
                                Column(
                                    modifier = Modifier.weight(1f).clickable { if (app.packageName in lockedApps) pendingLockedPackage = app.packageName else onLaunchApp(app.packageName) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = app.icon.asImageBitmap(),
                                        contentDescription = app.label,
                                        modifier = Modifier.size(39.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(app.label, color = Ink, fontSize = 9.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    if (favoriteLimitMessage) {
                        Spacer(Modifier.height(8.dp))
                        Text("A dock já tem cinco aplicativos. Remova um favorito para adicionar outro.", color = Rose, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueWash.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                            .clickable { wallpaperPicker.launch(arrayOf("image/*")) }
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = Blue)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("Seu cenário", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Escolha uma imagem da galeria", color = SoftInk, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                    Spacer(Modifier.height(12.dp))
                    Text("UM LUGAR PARA RESPIRAR", color = Color(0xFFB5AAB0), fontSize = 9.sp, letterSpacing = 1.8.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(White.copy(alpha = 0.96f), RoundedCornerShape(15.dp))
                                    .clickable {
                                        if (app.packageName in lockedApps) pendingLockedPackage = app.packageName
                                        else onLaunchApp(app.packageName)
                                    }
                                    .padding(horizontal = 15.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(bitmap = app.icon.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(38.dp))
                                Spacer(Modifier.size(12.dp))
                                Text(app.label, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                if (app.packageName in lockedApps) {
                                    Icon(Icons.Outlined.Lock, contentDescription = "Aplicativo protegido", tint = Blue, modifier = Modifier.padding(end = 8.dp).size(18.dp))
                                }
                                Icon(
                                    imageVector = if (app.packageName in favorites) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Fixar favorito",
                                    tint = if (app.packageName in favorites) Blue else SoftInk,
                                    modifier = Modifier.size(24.dp).clickable {
                                        favoriteLimitMessage = false
                                        favorites = if (app.packageName in favorites) {
                                            favorites - app.packageName
                                        } else if (favorites.size < 5) {
                                            favorites + app.packageName
                                        } else {
                                            favoriteLimitMessage = true
                                            favorites
                                        }
                                        onFavoritesChanged(favorites)
                                    }
                                )
                            }
                        }
                        if (favoriteLimitMessage) {
                            item {
                                Text(
                                    "A dock já tem cinco aplicativos. Remova um favorito para adicionar outro.",
                                    color = Rose,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (filteredApps.isEmpty()) {
                            item {
                                Text("Nenhum aplicativo encontrado.", color = SoftInk, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "CLARKLAUNCH  ·  0.5.0",
                    color = Color(0xFFB5AAB0),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
