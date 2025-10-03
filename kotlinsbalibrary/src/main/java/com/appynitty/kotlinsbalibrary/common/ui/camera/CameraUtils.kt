package com.appynitty.kotlinsbalibrary.common.ui.camera

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


object CameraUtils {

    private const val TAG = "CameraUtils"
    const val IMAGE_PATH = "imgPath"

    fun deleteTheFile(path: String) {
        val fDelete = File(path)
        if (fDelete.exists()) {
            if (fDelete.delete()) {
                Log.e(TAG, "file deleted!")
            } else {
                Log.e(TAG, "file is not deleted!")
            }
        }
    }

    @Throws(IOException::class, Resources.NotFoundException::class)
    fun getBase64Image(sourcePath: String?, bitmap: Bitmap): String {

        val encoded: String?
        if (TextUtils.isEmpty(sourcePath)) {
            return ""
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        val array = byteArrayOutputStream.toByteArray()
        val encode = Base64.encodeToString(array, Base64.DEFAULT)

        encoded = "data:image/jpeg;base64,$encode"

        byteArrayOutputStream.flush()
        byteArrayOutputStream.close()

        return encoded.replace("\\s".toRegex(), "")
    }

    fun prepareBase64Images(
        bitmap: Bitmap,
        dumpId: String,
        filePath: String,
        latitude: String?,
        longitude: String?,
        gcDate : String
    ): String {

        return try {
            // Call to add watermark
            CameraWatermark.WatermarkOptions()

            val newBitmap = CameraWatermark.addWatermark(
                bitmap, gcDate, latitude!!, longitude!!, dumpId
            )

            // Get Base64 encoded image from the new bitmap
            getBase64Image(filePath, newBitmap)
        } catch (e: Exception) {
            // Handle the exception and return a default value (e.g., empty string or error message)
            e.printStackTrace() // Optional: Log the error for debugging purposes
            ""  // Return an empty string or a predefined error message
        }

    }

    fun prepareBeforeAfterImages(
        beforeImagePath: String?,
        afterImagePath: String?,
        referenceId: String?,
        latitude: String?,
        longitude: String?,
        gcDate: String
    ): HashMap<String, String> {

        val imagesMap = HashMap<String, String>()
        val beforeImageBase64: String?
        val afterImageBase64: String?

        if (beforeImagePath != null) {
            if (beforeImagePath != "") {
                val bitmap = BitmapFactory.decodeFile(beforeImagePath)
                if (bitmap != null) {
                    beforeImageBase64 = prepareBase64Images(
                        bitmap, referenceId!!,
                        beforeImagePath, latitude, longitude,
                        gcDate
                    )
                    imagesMap["beforeImageBase64"] = beforeImageBase64.toString()
                }
            }

        }
        if (afterImagePath != null) {
            if (afterImagePath != "") {
                val bitmap = BitmapFactory.decodeFile(afterImagePath)
                if (bitmap != null) {
                    afterImageBase64 = prepareBase64Images(
                        bitmap, referenceId!!,
                        afterImagePath, latitude, longitude,
                        gcDate
                    )
                    imagesMap["afterImageBase64"] = afterImageBase64.toString()
                }
            }
        }
        return imagesMap
    }

}