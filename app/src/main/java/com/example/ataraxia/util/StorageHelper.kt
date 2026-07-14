package com.example.ataraxia.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object StorageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        return try {
            val directory = File(context.filesDir, "journal_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(directory, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getVoiceRecordFile(context: Context): File {
        val directory = File(context.filesDir, "journal_voice").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "audio_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.3gp"
        return File(directory, fileName)
    }

    fun deleteFile(path: String) {
        if (path.isNotEmpty()) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
