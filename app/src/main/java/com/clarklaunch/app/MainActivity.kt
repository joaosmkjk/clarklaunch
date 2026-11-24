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
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val Canvas = Color(0xFFF8F7F8)
private val White = Color(0xFFFFFFFF)
private val Ink = Color(0xFF28242A)
private val SoftInk = Color(0xFF77717A)
private val Rose = Color(0xFFB85E7A)
private val RoseWash = Color(0xFFF5E8ED)
private val Line = Color(0xFFECE7EA)

private data class LaunchableApp(
    val label: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = android.graphics.Color.rgb(248, 247, 248)
        window.navigationBarColor = android.graphics.Color.rgb(248, 247, 248)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Rose,
                    background = Canvas,
                    surface = White,
                    onPrimary = White,
                    onBackground = Ink,
                    onSurface = Ink
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

    val filteredApps = apps.filter { it.label.contains(search, ignoreCase = true) }

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
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
                        .background(White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = "Aplicativos",
                        tint = Rose
                    )
                }
            }

            Spacer(Modifier.height(54.dp))

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

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Rose
                )
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

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllApps) "BIBLIOTECA" else "SEU ESPAÇO",
                    color = SoftInk,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showAllApps) "VOLTAR" else "VER TODOS",
                    color = Rose,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White, RoundedCornerShape(20.dp))
                        .clickable { showAllApps = true }
                        .padding(19.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(RoseWash, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = Rose
                        )
                    }
                    Spacer(Modifier.height(13.dp))
                    Text(
                        text = "Todos os aplicativos",
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "${apps.size} aplicativos disponíveis",
                        color = SoftInk,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "UM LUGAR PARA RESPIRAR",
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
                                .background(White, RoundedCornerShape(15.dp))
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(horizontal = 15.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(RoseWash, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.label.take(1).uppercase(),
                                    color = Rose,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.size(12.dp))
                            Text(text = app.label, color = Ink, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "CLARKLAUNCH  ·  0.1.0",
                color = Color(0xFFB5AAB0),
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
