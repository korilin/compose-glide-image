package com.korilin.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.korilin.samples.glide.GlideAsyncImage


const val WEBP_URL = "https://mathiasbynens.be/demo/animated-webp-supported.webp"
const val STATIC_URL =
    "https://olimg.3dmgame.com/uploads/images/xiaz/2020/1023/1603430276493.jpg"

enum class NetTestImageType {
    AsyncPainter, GlideImage, Coil
}

val NetTestImageType.color get() = when(this) {
    NetTestImageType.AsyncPainter -> Color.Green
    NetTestImageType.GlideImage -> Color.Red
    NetTestImageType.Coil -> Color.Blue
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NetTestImage(tag: String? = null, model: Any?, type: NetTestImageType) {
    when (type) {
        NetTestImageType.AsyncPainter -> GlideAsyncImage(
            tag = tag,
            model = model,
            contentDescription = null,
            modifier = Modifier
                .height(20.dp)
                .wrapContentWidth()
                .background(type.color),
            contentScale = ContentScale.FillHeight,
        )

        NetTestImageType.GlideImage -> GlideImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .height(20.dp)
                .wrapContentWidth()
                .background(type.color),
            contentScale = ContentScale.FillHeight,
        )

        NetTestImageType.Coil -> AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .height(20.dp)
                .wrapContentWidth()
                .background(type.color),
            contentScale = ContentScale.FillHeight,
        )
    }
}