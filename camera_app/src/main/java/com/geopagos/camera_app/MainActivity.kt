package com.geopagos.camera_app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geopagos.camera_app.ui.theme.MultiProcessAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiProcessAppTheme {
                var isStarted by rememberSaveable { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ButtonsRow(Modifier, isStarted) {
                        if (isStarted) {
                            stopService(Intent(this, CameraService::class.java))
                            isStarted = false
                        } else if (checkPermissions()) {
                            startService(Intent(this, CameraService::class.java))
                            isStarted = true
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Toast.makeText(this, "CAMERA APP - TARJETA DETECTADA", Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun ButtonsRow(modifier: Modifier, isStarted: Boolean, callback: () -> Unit) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            SimpleButton(
                modifier = modifier
                    .weight(1f)
                    .padding(5.dp),
                title = if (!isStarted) "Start" else "Stop",
                callback = callback,
            )
        }
    }

    @Composable
    fun SimpleButton(modifier: Modifier = Modifier, title: String, callback: () -> Unit = {}) {
        Button(
            modifier = modifier,
            onClick = callback
        ) {
            Text(text = title)
        }
    }

    private fun checkPermissions(): Boolean {
        return if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            false
        } else if (!Settings.canDrawOverlays(this)) {
            overlayPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            false
        } else true
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
}

