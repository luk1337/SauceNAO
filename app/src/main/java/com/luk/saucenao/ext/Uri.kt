package com.luk.saucenao.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Uri.pngDataStream(context: Context): ByteArrayInputStream {
    val stream = ByteArrayOutputStream()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.contentResolver, this)
        ).compress(Bitmap.CompressFormat.PNG, 100, stream)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            .compress(Bitmap.CompressFormat.PNG, 100, stream)
    }

    return ByteArrayInputStream(stream.toByteArray())
}
