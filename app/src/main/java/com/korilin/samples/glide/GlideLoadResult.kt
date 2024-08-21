package com.korilin.samples.glide

import com.google.accompanist.drawablepainter.DrawablePainter

internal sealed interface GlideLoadResult {
    data object Loading : GlideLoadResult
    data object Error : GlideLoadResult
    data class Success(val painter: DrawablePainter) : GlideLoadResult {
        override fun toString(): String {
            return "Success(painter={ painterIntrinsic: ${painter.intrinsicSize}})"
        }
    }
}