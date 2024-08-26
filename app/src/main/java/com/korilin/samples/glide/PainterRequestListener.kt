package com.korilin.samples.glide

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.painter.Painter
import com.bumptech.glide.request.RequestListener

interface PainterRequestListener: RequestListener<Drawable> {
    fun onPainterReady(painter: Painter)
}
