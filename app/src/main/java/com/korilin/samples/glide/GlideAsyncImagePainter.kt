package com.korilin.samples.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


@Stable
private class GlideAsyncImagePainter(
    val loading: Painter?,
    val failure: Painter?,
    val scale: ContentScale,
    val listener: RequestListener<Drawable>?,
    val request: Context.() -> RequestBuilder<Drawable>,
) : Painter(), RememberObserver {

    private var painter by mutableStateOf(loading)
    private var alpha: Float by mutableFloatStateOf(DefaultAlpha)
    private var colorFilter: ColorFilter? by mutableStateOf(null)

    override val intrinsicSize: Size
        get() = painter?.intrinsicSize ?: Size.Zero

    private val glideSize = AsyncGlideSize()

    override fun DrawScope.onDraw() {
        glideSize.tryEmit(size)
        Logger.log("GlideAsyncImagePainter") { "onDraw $size $intrinsicSize" }
        (painter ?: loading)?.apply { draw(size, alpha, colorFilter) }
    }

    private var rememberJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var remembered = false

    override fun onRemembered() {
        (painter as? RememberObserver)?.onRemembered()
        remembered = true
    }

    override fun onForgotten() {
        stopRequest()
        (painter as? RememberObserver)?.onForgotten()
    }

    override fun onAbandoned() {
        stopRequest()
        (painter as? RememberObserver)?.onAbandoned()
    }

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    private fun stopRequest() {
        remembered = false
        rememberJob?.cancel()
        rememberJob = null
        // finish all flow target
        glideSize.tryEmit(Size.Unspecified)
    }

    /**
     * @see <a href="https://github.com/bumptech/glide/blob/master/integration/compose/src/main/java/com/bumptech/glide/integration/compose/GlideAsyncImage.kt#L407">GlideImage</a>
     */
    private fun RequestBuilder<Drawable>.setupScaleTransform(): RequestBuilder<Drawable> {
        return when (scale) {
            ContentScale.Crop -> optionalCenterCrop()

            // Outside compose, glide would use fitCenter() for FIT. But that's probably not a good
            // decision given how unimportant Bitmap re-use is relative to minimizing texture sizes now.
            // So instead we'll do something different and prefer not to upscale, which means using
            // centerInside(). The UI can still scale the view even if the Bitmap is smaller.
            ContentScale.Fit,
            ContentScale.FillHeight,
            ContentScale.FillWidth,
            ContentScale.FillBounds -> optionalCenterInside()

            ContentScale.Inside -> optionalCenterInside()

            // NONE
            else -> this
        }
    }

    fun startRequest(scope: CoroutineScope, context: Context, model: Any?) {
        painter = loading
        rememberJob = (scope + Dispatchers.Main.immediate).launch {
            request(context)
                .setupScaleTransform()
                .load(model)
                .flow(glideSize, listener)
                .collectLatest {
                    Logger.log("GlideAsyncImagePainter") { "result $remembered $model -> $it" }
                    if (!remembered) return@collectLatest
                    val older = painter
                    painter = when (it) {
                        is GlideLoadResult.Loading -> loading
                        is GlideLoadResult.Error -> failure
                        is GlideLoadResult.Success -> it.painter
                    }
                    if (older != painter) {
                        (older as? RememberObserver)?.onForgotten()
                        (painter as? RememberObserver)?.onRemembered()
                    }
                }
        }
    }
}

@Composable
internal fun rememberGlideAsyncImagePainter(
    model: Any?,
    scale: ContentScale,
    loading: Painter? = null,
    failure: Painter? = null,
    listener: RequestListener<Drawable>? = null,
    request: (Context) -> RequestBuilder<Drawable> = { Glide.with(it).asDrawable() },
): Painter {
    val context = LocalContext.current
    val painter = remember {
        GlideAsyncImagePainter(
            loading = loading,
            failure = failure,
            scale = scale,
            listener = listener,
            request = request
        )
    }
    LaunchedEffect(model) {
        painter.startRequest(this, context, model)
    }
    return painter
}