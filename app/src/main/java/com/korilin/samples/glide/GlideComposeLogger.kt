package com.korilin.samples.glide

import android.util.Log
import com.bumptech.glide.load.engine.GlideException

object GlideComposeConfig {
    var loggerEnable = true
}



internal object Logger {
    fun log(tag: String, message: String) {
        if (GlideComposeConfig.loggerEnable) Log.d(tag, message)
    }

    fun error(tag: String, exception: GlideException?) {
        exception?.logRootCauses(tag)
    }
}