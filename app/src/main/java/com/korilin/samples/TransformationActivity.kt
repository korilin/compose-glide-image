package com.korilin.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korilin.samples.ui.theme.ComposeglideimageTheme

class TransformationActivity: ComponentActivity() {

    companion object {
        fun start(
            context: Context,
        ) {
            val intent = Intent(context, TransformationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()

        setContent {
            ComposeglideimageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Contents()
                    }
                }
            }
        }
    }

    private val state1 = mutableStateOf<String?>(null)
    private val state2 = mutableStateOf<String?>(WEBP_URL)

    @Composable
    private fun BoxScope.Contents() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            for (type in NetTestImageType.entries) {
                Text(type.name)
                NetTestImage(
                    model = state1.value,
                    type = type
                )
                Spacer(modifier = Modifier.height(10.dp))
            }


            for (type in NetTestImageType.entries) {
                Text(type.name)
                NetTestImage(
                    model = state2.value,
                    type = type
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Button(
            onClick = {
                state1.value = WEBP_URL
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Load")
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Box(
        modifier = Modifier.height(100.dp)
    ){
        Image(
            painter = painterResource(R.drawable.img),
            contentDescription = null,
            modifier = Modifier.fillMaxHeight().aspectRatio(0.8f),
            alignment = Alignment.CenterStart,
            contentScale = ContentScale.FillHeight
        )
    }
}