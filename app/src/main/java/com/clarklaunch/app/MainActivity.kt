package com.clarklaunch.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

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
                    apps = loadLaunchableApps(),
                    initialFavorites = getSavedFavorites(),
                    initialWallpaper = getSavedWallpaper(),
                    onLaunchApp = ::launchApp,
                    onFavoritesChanged = ::saveFavorites,
                    onWallpaperChanged = ::saveWallpaper
                )
            }
        }
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
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun getSavedFavorites(): Set<String> =
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .getStringSet("favorites", emptySet())?.toSet() ?: emptySet()

    private fun saveFavorites(favorites: Set<String>) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putStringSet("favorites", favorites).apply()
    }

    private fun getSavedWallpaper(): String? =
        getSharedPreferences("clarklaunch", MODE_PRIVATE).getString("wallpaper_uri", null)

    private fun saveWallpaper(uri: String) {
        getSharedPreferences("clarklaunch", MODE_PRIVATE)
            .edit().putString("wallpaper_uri", uri).apply()
    }
}

@Composable
private fun ClarkLaunchHome(
    apps: List<LaunchableApp>,
    initialFavorites: Set<String>,
    initialWallpaper: String?,
    onLaunchApp: (String) -> Unit,
    onFavoritesChanged: (Set<String>) -> Unit,
    onWallpaperChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var showAllApps by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(initialFavorites) }
    var wallpaperUri by remember { mutableStateOf(initialWallpaper?.let(Uri::parse)) }

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
                // Some document providers do not offer persistable permissions.
            }
            wallpaperUri = uri
            onWallpaperChanged(uri.toString())
        }
    }

    val wallpaperBitmap = remember(wallpaperUri) {
        wallpaperUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    val filteredApps = apps.filter { it.label.contains(search, ignoreCase = true) }
    val favoriteApps = apps.filter { it.packageName in favorites }.take(5)

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (wallpaperBitmap != null) {
                Image(
                    bitmap = wallpaperBitmap,
                    contentDescription = "Papel de parede",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.20f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Canvas.copy(alpha = 0.76f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 25.dp, vertical = 25.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "C L A R K",
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.8.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "a quieter kind of home",
                            color = SoftInk,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(BlueWash, CircleShape)
                            .clickable { wallpaperPicker.launch(arrayOf("image/*")) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Escolher papel de parede",
                            tint = Blue
                        )
                    }
                }

                Spacer(Modifier.height(45.dp))

                Text(
                    text = "Leveza em",
                    color = Ink,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "cada detalhe.",
                    color = Rose,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Seu mundo, em harmonia.",
                    color = SoftInk,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(26.dp))

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
                                if (search.isEmpty()) {
                                    Text("Buscar um aplicativo", color = SoftInk, fontSize = 13.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAllApps) "BIBLIOTECA" else "SEUS FAVORITOS",
                        color = SoftInk,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showAllApps) "VOLTAR" else "VER TODOS",
                        color = Blue,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable {
                            showAllApps = !showAllApps
                            if (!showAllApps) search = ""
                        }
                    )
                }

                Spacer(Modifier.height(13.dp))

                if (!showAllApps) {
                    if (favoriteApps.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White.copy(alpha = 0.94f), RoundedCornerShape(20.dp))
                                .clickable { showAllApps = true }
                                .padding(19.dp)
                        ) {
                            Text(
                                "Monte sua dock",
                                color = Ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Abra todos os aplicativos e toque na estrela para fixar seus favoritos.",
                                color = SoftInk,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White.copy(alpha = 0.94f), RoundedCornerShape(22.dp))
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            favoriteApps.forEachIndexed { index, app ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onLaunchApp(app.packageName) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = app.icon.asImageBitmap(),
                                        contentDescription = app.label,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(Modifier.height(7.dp))
                                    Text(
                                        text = app.label,
                                        color = Ink,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueWash.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                            .clickable { wallpaperPicker.launch(arrayOf("image/*")) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = Blue)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("Papel de parede", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Escolha uma imagem da sua galeria", color = SoftInk, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "UM LUGAR PARA RESPIRAR",
                        color = Color(0xFFB5AAB0),
                        fontSize = 9.sp,
                        letterSpacing = 1.8.sp
                    )
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
                                    .clickable { onLaunchApp(app.packageName) }
                                    .padding(horizontal = 15.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = app.icon.asImageBitmap(),
                                    contentDescription = app.label,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(Modifier.size(12.dp))
                                Text(
                                    text = app.label,
                                    color = Ink,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (app.packageName in favorites) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Fixar favorito",
                                    tint = if (app.packageName in favorites) Blue else SoftInk,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            favorites = if (app.packageName in favorites) {
                                                favorites - app.packageName
                                            } else {
                                                (favorites + app.packageName).toSet()
                                            }
                                            onFavoritesChanged(favorites)
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "CLARKLAUNCH  ·  0.2.0",
                    color = Color(0xFFB5AAB0),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
