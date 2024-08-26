package com.korilin.samples.glide

import androidx.compose.ui.graphics.painter.Painter

internal sealed interface GlideLoadResult {
    data object Error : GlideLoadResult
    data class Success(val painter: Painter) : GlideLoadResult {
        override fun toString(): String {
            return "Success($painter={ painterIntrinsic: ${painter.intrinsicSize}})"
        }
    }
}