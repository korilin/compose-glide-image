package com.korilin.samples.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestListener

@Composable
fun GlideAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    loading: Painter? = null,
    failure: Painter? = null,
    listener: RequestListener<Drawable>? = null,
    request: (Context) -> RequestBuilder<Drawable> = { Glide.with(it).asDrawable() },
) {
    val painter = when (model) {
        is Painter -> model
        is Int -> painterResource(model)
        else -> rememberGlideAsyncImagePainter(
            model, contentScale, loading, failure, listener, request
        )
    }

    Layout(
        modifier = modifier
            .asyncGlideNode(
                painter,
                contentDescription,
                alignment,
                contentScale,
                alpha,
                colorFilter
            ),
        measurePolicy = { _, constraints ->
            layout(constraints.minWidth, constraints.minHeight) {}
        },
    )
}