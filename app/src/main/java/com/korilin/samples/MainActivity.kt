package com.korilin.samples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korilin.samples.ui.theme.ComposeglideimageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeglideimageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CustomGlideList(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private const val WEBP_URL = "https://mathiasbynens.be/demo/animated-webp-supported.webp"

@Composable
fun CustomGlideList(name: String, modifier: Modifier = Modifier) {
    LazyColumn {
        items(100) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(
                    text = it.toString()
                )

                Image(
                    painter = rememberGlideAsyncImagePainter(
                        "$WEBP_URL?id=$it"
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth(),
                    contentScale = ContentScale.FillHeight,
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeglideimageTheme {
        CustomGlideList("Android")
    }
}