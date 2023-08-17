package com.geopagos.multi_process_app

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.os.Messenger
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ProcessLifecycleOwner
import com.geopagos.multi_process_app.ui.theme.MultiProcessAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MyViewModel by viewModels()
    private val incomingHandler by lazy { HandlerImpl(viewModel) }
    private val connection by lazy { ServiceConnectionImpl(viewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Process lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(DefaultLifecycleObserverImpl("PROCESS", Color.Blue, viewModel::sendState))
        //Activity lifecycle
        lifecycle.addObserver(DefaultLifecycleObserverImpl("ACTIVITY", Color.Red, viewModel::sendState))

        viewModel.messages.observeForever {
            sendMessageToService(it.first, it.second)
        }

        viewModel.serviceState.observe(this) {
            when (it) {
                ServiceState.START -> {
                    Intent(this@MainActivity, MyService::class.java).also { intent ->
                        bindService(intent, connection, BIND_AUTO_CREATE)
                    }
                }

                ServiceState.STOP -> {
                    unbindService(connection)
                }
            }
        }

        setContent {
            MultiProcessAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Log(Modifier, viewModel.log.observeAsState().value)
                }
            }
        }
    }

    private fun sendMessageToService(what: Int, data: Bundle = Bundle()) {
        connection.service?.let { service ->
            Messenger(service).send(
                Message().apply {
                    this.what = what
                    this.replyTo = Messenger(incomingHandler)
                    this.data = data
                })
        }
    }

    @Composable
    fun Log(modifier: Modifier, log: List<Pair<Color, String>>?) {
        Column {
            ButtonsRow(modifier = modifier)

            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(log ?: listOf(Color.Black to "Empty")) {
                    Text(
                        color = it.first,
                        text = it.second,
                        modifier = modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    @Composable
    fun ButtonsRow(modifier: Modifier) {
        val isServiceEnabled = viewModel.isServiceStarted.observeAsState(false).value

        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            SimpleButton(
                modifier = modifier
                    .weight(1f)
                    .padding(5.dp),
                title = "Start",
                enabled = !isServiceEnabled,
                callback = viewModel::startService,
            )

            SimpleButton(
                modifier = modifier
                    .weight(1f)
                    .padding(5.dp),
                title = "Stop",
                enabled = isServiceEnabled,
                callback = viewModel::stopService
            )

            SimpleButton(
                modifier = modifier
                    .weight(2f)
                    .padding(5.dp),
                title = "Launch activity"
            ) {
                Intent(this@MainActivity, OtherActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
        }
    }

    @Composable
    fun SimpleButton(modifier: Modifier = Modifier, title: String, enabled: Boolean = true, callback: () -> Unit = {}) {
        Button(
            modifier = modifier,
            onClick = callback,
            enabled = enabled
        ) {
            Text(text = title)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LogPreview() {
        val log = listOf(
            Color.Black to "Atlanta",
            Color.Gray to "Atlanta",
            Color.Blue to "Atlanta",
            Color.Red to "Atlanta",
        )

        MultiProcessAppTheme {
            Log(Modifier, log)
        }
    }
}
