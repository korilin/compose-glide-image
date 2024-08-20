package com.korilin.samples

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.math.roundToInt


object GlideAsyncImageConfig {
    var loggerEnable = true
}

private object Logger {
    fun log(tag: String, message: String) {
        if (GlideAsyncImageConfig.loggerEnable) Log.d(tag, message)
    }

    fun error(tag: String, exception: GlideException?) {
        exception?.logRootCauses(tag)
    }
}

private data class GlideSize(val width: Int, val height: Int)

private sealed interface ResolvableGlideSize {
    suspend fun getSize(): GlideSize
}

private data class ImmediateGlideSize(val size: GlideSize) : ResolvableGlideSize {
    override suspend fun getSize(): GlideSize {
        return size
    }
}

private class AsyncGlideSize : ResolvableGlideSize {
    private val sizeFlow = MutableSharedFlow<Flow<Size>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun connect(size: Flow<Size>) {
        this.sizeFlow.tryEmit(size)
    }

    private fun Float.roundFiniteToInt() = if (isFinite()) roundToInt() else Target.SIZE_ORIGINAL

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSize(): GlideSize {
        return sizeFlow
            .transformLatest { emitAll(it) }
            .mapNotNull {
                when {
                    it.isUnspecified -> GlideSize(
                        width = Target.SIZE_ORIGINAL,
                        height = Target.SIZE_ORIGINAL
                    )

                    else -> GlideSize(
                        width = it.width.roundFiniteToInt(),
                        height = it.height.roundFiniteToInt()
                    )
                }
            }.first()
    }
}

private sealed interface GlideLoadResult {
    data object Loading : GlideLoadResult
    data object Error : GlideLoadResult
    data class Success(val painter: DrawablePainter) : GlideLoadResult {
        override fun toString(): String {
            return "Success(painter={ painterIntrinsic: ${painter.intrinsicSize}})"
        }
    }
}

private fun RequestBuilder<Drawable>.flow(
    size: ResolvableGlideSize,
    listener: RequestListener<Drawable>?,
): Flow<GlideLoadResult> {
    return callbackFlow {
        val target = FlowTarget(this, size, listener)
        listener(target).into(target)
        awaitClose { manager.clear(target) }
    }
}

private class FlowTarget(
    private val scope: ProducerScope<GlideLoadResult>,
    private val size: ResolvableGlideSize,
    private val listener: RequestListener<Drawable>?,
) : Target<Drawable>, RequestListener<Drawable> {

    private val Drawable.width: Int
        get() = (this as? BitmapDrawable)?.bitmap?.width ?: intrinsicWidth

    private val Drawable.height: Int
        get() = (this as? BitmapDrawable)?.bitmap?.height ?: intrinsicHeight

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>,
        isFirstResource: Boolean
    ): Boolean {
        Logger.error("FlowTarget", e)
        listener?.onLoadFailed(e, model, target, isFirstResource)
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>?,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        Logger.log(
            "FlowTarget",
            "onResourceReady first:$isFirstResource source:$dataSource Size(${resource.width}, ${resource.height}) model:$model "
        )
        listener?.onResourceReady(resource, model, target, dataSource, isFirstResource)
        return false
    }

    override fun getSize(cb: SizeReadyCallback) {
        scope.launch {
            val complete = size.getSize()
            Logger.log("FlowTarget", "getSize $complete")
            cb.onSizeReady(complete.width, complete.height)
        }
    }

    @Volatile
    var currentRequest: Request? = null
    override fun setRequest(request: Request?) {
        currentRequest = request
    }

    override fun getRequest(): Request? {
        return currentRequest
    }

    override fun removeCallback(cb: SizeReadyCallback) {}
    override fun onLoadCleared(placeholder: Drawable?) {}
    override fun onLoadFailed(errorDrawable: Drawable?) {
        scope.trySend(GlideLoadResult.Error)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        val painter = DrawablePainter(resource.mutate())
        scope.trySend(GlideLoadResult.Success(painter))
    }

    override fun onStart() {}
    override fun onStop() {}
    override fun onDestroy() {}
    override fun onLoadStarted(placeholder: Drawable?) {}
}

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