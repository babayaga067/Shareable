package com.example.sangeet.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class FileUploadUtil {
    
    companion object {
        
        fun uploadImage(
            context: Context,
            imageUri: Uri,
            callback: (Boolean, String?) -> Unit
        ) {
            try {
                val fileName = "image_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(file)
                
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                callback(true, file.absolutePath)
            } catch (e: Exception) {
                callback(false, null)
            }
        }
        
        fun uploadAudio(
            context: Context,
            audioUri: Uri,
            callback: (Boolean, String?) -> Unit
        ) {
            try {
                val fileName = "audio_${UUID.randomUUID()}.mp3"
                val file = File(context.filesDir, fileName)
                
                val inputStream: InputStream? = context.contentResolver.openInputStream(audioUri)
                val outputStream = FileOutputStream(file)
                
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                callback(true, file.absolutePath)
            } catch (e: Exception) {
                callback(false, null)
            }
        }
    }
}