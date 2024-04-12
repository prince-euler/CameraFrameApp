package com.example.cameraframeapp

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.cameraframeapp.ui.theme.CameraFrameAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val imageDrawableResId = R.drawable.image3
        val imageBitmap = BitmapFactory.decodeResource(resources, imageDrawableResId)

        val networkUtil = NetworkUtil(this)
        val file = viewModel.saveImageToFile(this@MainActivity, imageBitmap)

        networkUtil.observe(this@MainActivity) { networkState ->
            lifecycleScope.launch {
                when (networkState) {
                    NetworkState.OKINTERNET -> {
                        lifecycleScope.launch {
                            viewModel.sendImageToServer(file)
                            delay(2000)
                            file.delete()
                        }
                    }

                    NetworkState.NOINTERNET -> {
                        // Log if no internet
                        Log.d("data-response", "Network lost")
                    }

                    else -> {
                        // Log if internet is slow
                        Log.d("data-response", "Oops seems like your internet is slow...")
                    }
                }
            }

        }
        setContent {
            CameraFrameAppTheme {
                ImageUploader(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ImageUploader(viewModel: MainViewModel) {

    val status by viewModel.status.collectAsState("")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = status
        )
    }
}
