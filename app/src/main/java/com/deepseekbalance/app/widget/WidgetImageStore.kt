package com.deepseekbalance.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException

class WidgetImageStore(context: Context) {
    private val appContext = context.applicationContext
    private val imageFile = File(appContext.filesDir, IMAGE_FILE_NAME)

    fun hasImage(): Boolean = imageFile.isFile && imageFile.length() > 0L

    fun saveFromUri(uri: Uri): Boolean {
        return try {
            val bitmap = readOrientedBitmap(uri) ?: return false
            val successfullySaved = imageFile.outputStream().buffered().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, STORED_IMAGE_QUALITY, output)
            }
            bitmap.recycle()

            if (!successfullySaved) {
                imageFile.delete()
            }
            successfullySaved
        } catch (_: IOException) {
            imageFile.delete()
            false
        } catch (_: SecurityException) {
            imageFile.delete()
            false
        }
    }

    fun clear() {
        imageFile.delete()
    }

    fun loadWidgetBitmap(): Bitmap? {
        if (!hasImage()) {
            return null
        }

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFile.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val sampleSize = WidgetImageSizer.calculateInSampleSize(
            sourceWidth = bounds.outWidth,
            sourceHeight = bounds.outHeight,
            targetWidth = WidgetImageSizer.WIDGET_IMAGE_MAX_WIDTH,
            targetHeight = WidgetImageSizer.WIDGET_IMAGE_MAX_HEIGHT,
        )
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val decoded = BitmapFactory.decodeFile(imageFile.absolutePath, options) ?: return null
        val (targetWidth, targetHeight) = WidgetImageSizer.fitWithin(
            sourceWidth = decoded.width,
            sourceHeight = decoded.height,
            maxWidth = WidgetImageSizer.WIDGET_IMAGE_MAX_WIDTH,
            maxHeight = WidgetImageSizer.WIDGET_IMAGE_MAX_HEIGHT,
        )
        if (targetWidth == decoded.width && targetHeight == decoded.height) {
            return decoded
        }

        return Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true).also {
            decoded.recycle()
        }
    }

    private fun readOrientedBitmap(uri: Uri): Bitmap? {
        val contentResolver = appContext.contentResolver
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val sampleSize = WidgetImageSizer.calculateInSampleSize(
            sourceWidth = bounds.outWidth,
            sourceHeight = bounds.outHeight,
            targetWidth = WidgetImageSizer.STORED_IMAGE_MAX_DIMENSION,
            targetHeight = WidgetImageSizer.STORED_IMAGE_MAX_DIMENSION,
        )
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val orientation = runCatching {
            contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val oriented = applyOrientation(decoded, orientation)
        val (targetWidth, targetHeight) = WidgetImageSizer.fitWithin(
            sourceWidth = oriented.width,
            sourceHeight = oriented.height,
            maxWidth = WidgetImageSizer.STORED_IMAGE_MAX_DIMENSION,
            maxHeight = WidgetImageSizer.STORED_IMAGE_MAX_DIMENSION,
        )
        if (targetWidth == oriented.width && targetHeight == oriented.height) {
            return oriented
        }

        return Bitmap.createScaledBitmap(oriented, targetWidth, targetHeight, true).also {
            oriented.recycle()
        }
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        ).also {
            if (it !== bitmap) {
                bitmap.recycle()
            }
        }
    }

    private companion object {
        const val IMAGE_FILE_NAME = "widget_background.jpg"
        const val STORED_IMAGE_QUALITY = 90
    }
}
