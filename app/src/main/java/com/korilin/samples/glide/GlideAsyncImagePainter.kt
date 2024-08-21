package com.korilin.samples.glide

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.trace
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.ktx.flow
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.manager
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.accompanist.drawablepainter.DrawablePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


@Stable
private class GlideAsyncImagePainter(
    val model: Any?,
    val context: Context,
    val scope: CoroutineScope,
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
        get() = painter?.intrinsicSize ?: Size.Unspecified

    private val drawSize = MutableSharedFlow<Size>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun DrawScope.onDraw() {
        drawSize.tryEmit(size)
        (painter ?: loading)?.apply { draw(size, alpha, colorFilter) }
    }

    private var rememberJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var remembered = false

    override fun onRemembered() = trace("GlideAsyncImagePainter.onRemembered") {
        (painter as? RememberObserver)?.onRemembered()
        remembered = true
        rememberJob = (scope + Dispatchers.Main.immediate).launch {
            startRequest()
        }
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
    }

    /**
     * @see <a href="https://github.com/bumptech/glide/blob/master/integration/compose/src/main/java/com/bumptech/glide/integration/compose/GlideImage.kt#L407">GlideImage</a>
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

    private suspend fun startRequest() {
        val size = AsyncGlideSize()
        size.connect(drawSize)

        request(context)
            .setupScaleTransform()
            .load(model)
            .flow(size, listener)
            .collectLatest {
                Logger.log("GlideAsyncImagePainter", "result $remembered $model -> $it")
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

@Composable
internal fun rememberGlideAsyncImagePainter(
    model: Any?,
    scale: ContentScale,
    loading: Painter? = null,
    failure: Painter? = null,
    listener: RequestListener<Drawable>? = null,
    request: (Context) -> RequestBuilder<Drawable> = { Glide.with(it).asDrawable() },
): Painter {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    return remember(model) {
        GlideAsyncImagePainter(
            model = model,
            context = context,
            scope = scope,
            loading = loading,
            failure = failure,
            scale = scale,
            listener = listener,
            request = request
        )
    }
}