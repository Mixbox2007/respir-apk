package app.respir

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var prefs: Prefs

    private val notifyPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        prefs.ensureClock()
        setContent { RespirApp() }
    }

    @Composable
    private fun RespirApp() {
        var enabled by remember { mutableStateOf(prefs.enabled) }
        var resisted by remember { mutableIntStateOf(prefs.resisted) }
        var slips by remember { mutableIntStateOf(prefs.slips) }
        var lastCig by remember { mutableLongStateOf(prefs.lastCigaretteAt) }
        val interval = prefs.intervalMinutes

        MaterialTheme(
            colorScheme = darkColorScheme(
                background = Color(0xFF0C0F0D),
                surface = Color(0xFF151A17),
                primary = Color(0xFFC5D1C8),
                onPrimary = Color(0xFF101412),
                onBackground = Color(0xFFECEEEA),
                onSurface = Color(0xFFECEEEA),
            ),
        ) {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Respir", fontSize = 36.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Cette heure, tu ne fumes pas.",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                    )
                    Text(
                        "Rappel toutes les $interval minutes. Conçu pour Galaxy Z Fold / One UI 8.",
                        color = Color(0xFF9AA49C),
                    )
                    val hours = ((System.currentTimeMillis() - lastCig) / 3_600_000L).coerceAtLeast(0)
                    Text("Sans cigarette : ${hours} h", fontSize = 18.sp)
                    Text("Envies tenues : $resisted   ·   Glissades : $slips")

                    Button(
                        onClick = {
                            if (!enabled) {
                                askPermissions()
                                prefs.enabled = true
                                ReminderScheduler.schedule(this@MainActivity, interval)
                                enabled = true
                            } else {
                                prefs.enabled = false
                                ReminderScheduler.cancel(this@MainActivity)
                                enabled = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (enabled) "Couper les rappels" else "Activer chaque heure")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = {
                                resisted += 1
                                prefs.resisted = resisted
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("J'ai résisté") }
                        OutlinedButton(
                            onClick = {
                                slips += 1
                                prefs.slips = slips
                                lastCig = System.currentTimeMillis()
                                prefs.lastCigaretteAt = lastCig
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("J'ai fumé") }
                    }

                    OutlinedButton(
                        onClick = { openBatterySettings() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Autoriser la batterie (One UI)")
                    }
                    Text(
                        "Sur Samsung : Applications → Respir → Batterie → Non restreint. " +
                            "Sinon One UI tue les alarmes.",
                        color = Color(0xFF9AA49C),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notifyPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                })
            }
        }
    }

    private fun openBatterySettings() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        } else {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        }
    }
}
