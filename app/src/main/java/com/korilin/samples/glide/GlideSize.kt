package com.korilin.samples.glide

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlin.math.roundToInt


internal data class GlideSize(val width: Int, val height: Int)


internal sealed interface ResolvableGlideSize {
    suspend fun getSize(): GlideSize
}

internal data class ImmediateGlideSize(val size: GlideSize) : ResolvableGlideSize {
    override suspend fun getSize(): GlideSize {
        return size
    }
}

internal class AsyncGlideSize : ResolvableGlideSize {
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