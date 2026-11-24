package com.clarklaunch.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Wine = Color(0xFF842D49)
private val Background = Color(0xFF100D10)
private val Panel = Color(0xFF1D171C)
private val Muted = Color(0xFFAA9DA4)

private data class LaunchableApp(
    val label: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Wine,
                    background = Background,
                    surface = Panel,
                    onPrimary = Color.White,
                    onBackground = Color(0xFFF5EDF1),
                    onSurface = Color(0xFFF5EDF1)
                )
            ) {
                ClarkLaunchHome(
                    apps = loadLaunchableApps(),
                    onLaunchApp = ::launchApp
                )
            }
        }
    }

    private fun loadLaunchableApps(): List<LaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                LaunchableApp(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName
                )
            }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

@Composable
private fun ClarkLaunchHome(
    apps: List<LaunchableApp>,
    onLaunchApp: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showAllApps by remember { mutableStateOf(false) }

    val filteredApps = apps.filter {
        it.label.contains(search, ignoreCase = true)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CLARK",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "SYSTEM / HOME",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Wine.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = "ClarkLaunch",
                        tint = Color(0xFFE5A9BE)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = "Seu espaço.",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "Suas regras.",
                color = Color(0xFFE5A9BE),
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Tudo o que você precisa, sem excesso.",
                color = Muted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel, RoundedCornerShape(16.dp))
                    .padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Muted
                )
                Spacer(Modifier.size(10.dp))
                BasicTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        if (it.isNotBlank()) showAllApps = true
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    decorationBox = { innerTextField ->
                        Box {
                            if (search.isEmpty()) {
                                Text("Buscar aplicativos", color = Muted, fontSize = 14.sp)
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
                    text = if (showAllApps) "APLICATIVOS" else "SEU DISPOSITIVO",
                    color = Muted,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showAllApps) "INÍCIO" else "VER TODOS",
                    color = Color(0xFFE5A9BE),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Panel, RoundedCornerShape(18.dp))
                        .clickable { showAllApps = true }
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Biblioteca de aplicativos",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "${apps.size} aplicativos encontrados · Toque para explorar",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
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
                                .background(Panel, RoundedCornerShape(14.dp))
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Wine.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.label.take(1).uppercase(),
                                    color = Color(0xFFE5A9BE),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "CLARKLAUNCH  •  FOUNDATION 0.1.0",
                color = Color(0xFF74666D),
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
