package com.korilin.samples.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.trace
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestListener

/**
 * Async image load node base on glide.
 *
 * @param model Support Url string, Uri, Drawable ResId, or [Painter]
 * @param tag Log tag used to locate problems
 */
@Composable
fun GlideAsyncImage(
    model: Any?,
    tag: String? = null,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    loading: Painter? = null,
    failure: Painter? = null,
    listener: RequestListener<Drawable>? = null,
    requestBuilder: (Context) -> RequestBuilder<Drawable> = { Glide.with(it).asDrawable() },
) = trace("GlideAsyncImage") {
    val painter = when (model) {
        is Painter -> model
        is Int -> painterResource(model)
        else -> null
    }

    val context = LocalContext.current

    val nodeModel = remember(model) {
        if (painter != null) PainterModel(painter)
        else GlideRequestModel(
            model = model,
            requestBuilder = requestBuilder(context),
            listener = listener
        )
    }

    Layout(
        modifier = modifier
            .glidePainterNode(
                tag = tag,
                nodeModel = nodeModel,
                loadingPainter = loading,
                failurePainter = failure,
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