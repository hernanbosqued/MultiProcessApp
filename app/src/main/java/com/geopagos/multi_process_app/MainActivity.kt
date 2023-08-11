package com.geopagos.multi_process_app

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ProcessLifecycleOwner
import com.geopagos.multi_process_app.ui.theme.MultiProcessAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MyViewModel by viewModels()
    private val incomingHandler by lazy { HandlerImpl(viewModel) }
    private val connection by lazy { ServiceConnectionImpl(viewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(DefaultLifecycleObserverImpl(viewModel::sendState))
        viewModel.messages().observeForever {
            sendMessage(it.first, it.second)
        }

        setContent {
            MultiProcessAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Log(Modifier, viewModel)
                }
            }
        }
    }

    private fun sendMessage(what: Int, data: Bundle = Bundle()) {
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
    fun Log(modifier: Modifier, viewModel: MyViewModel) {
        val log = viewModel.log().observeAsState()

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
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(log.value ?: listOf("Empty")) {
                    Text(
                        text = it,
                        modifier = modifier.fillMaxSize()
                    )
                }
            }
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
}
