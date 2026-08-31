package com.mimica.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ColorExtractor {

    suspend fun extractColors(bitmap: Bitmap): Palette? = withContext(Dispatchers.Default) {
        try {
            Palette.from(bitmap).generate()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun extractPaletteFromUri(context: Context, uriString: String?): Palette? = withContext(Dispatchers.IO) {
        if (uriString.isNullOrEmpty()) return@withContext null
        try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uriString)
                .allowHardware(false) // Required for software Bitmap access
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    return@withContext extractColors(bitmap)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
