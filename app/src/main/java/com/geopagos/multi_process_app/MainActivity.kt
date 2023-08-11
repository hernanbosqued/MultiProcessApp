package com.geopagos.multi_process_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geopagos.multi_process_app.ui.theme.MultiProcessAppTheme

class MainActivity : ComponentActivity() {

    private val list = mutableStateListOf<String>()

    private val incomingHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_COUNTER -> {
                    val counter = msg.data.getString("log").orEmpty()
                    val pid = msg.data.getString("pid").orEmpty()
                    list.add("$pid->$counter")
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            Messenger(service).send(
                Message().apply {
                    what = MSG_START
                    replyTo = Messenger(incomingHandler)
                    data = Bundle().apply {
                        putString("log", "Atlanta")
                    }
                })

            list.add("Connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            list.add("Disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiProcessAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Log(list = list)
                }
            }
        }
    }

    @Composable
    fun Log(modifier: Modifier = Modifier, list: List<String>) {
        val notesList = remember { list }

        Column {
            Row(
                modifier = modifier.fillMaxWidth()
            ) {
                SimpleButton(
                    modifier = modifier
                        .weight(1f)
                        .padding(5.dp),
                    title = "Start"
                )
                {
                    Intent(this@MainActivity, MyService::class.java).also { intent ->
                        bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    }
                }

                SimpleButton(
                    modifier = modifier
                        .weight(1f)
                        .padding(5.dp),
                    title = "Stop"
                )
                {
                    this@MainActivity.unbindService(connection)
                }

            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 25.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "\uD83C\uDF3F  Plants in Cosmetics",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                items(notesList.reversed()) {
                    Text(
                        text = it,
                        modifier = modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MultiProcessAppTheme {
            Log(list = emptyList())
        }
    }

    @Composable
    fun SimpleButton(modifier: Modifier = Modifier, title: String = "Title", callback: () -> Unit = {}) {
        Button(
            modifier = modifier,
            onClick = callback
        ) {
            Text(text = title)
        }
    }
}
