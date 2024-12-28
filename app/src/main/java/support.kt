package com.example.myapp.services

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.coroutines.CoroutineCallback
import io.appwrite.models.File
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.sql.Timestamp
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.io.File as JavaFile


object Appwrite {
    private const val ENDPOINT = "https://cloud.appwrite.io/v1"
    private const val PROJECT_ID = "67670772002be5d99467"
    private const val BUCKET_ID = "676e56da0031185cb7b1"

    private lateinit var client: Client
    private val firestore = FirebaseFirestore.getInstance()

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)
    }

    fun getClient(): Client {
        if (!::client.isInitialized) {
            throw IllegalStateException("Client is not initialized. Call init() first.")
        }
        return client
    }

    fun saveImageOnly(
        context: Context,
        imageUri: Uri,
        userId: String,
        userName: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageFile = uriToFile(context, imageUri)

                val storage = Storage(client)

                val inputFile = InputFile.fromFile(imageFile)

                val result = storage.createFile(
                    bucketId = BUCKET_ID,
                    fileId = ID.unique(),
                    file = inputFile
                )

                val fileUrl = "https://cloud.appwrite.io/v1/storage/buckets/676e56da0031185cb7b1/files/"+result.id+"/view?project=67670772002be5d99467&project=67670772002be5d99467&mode=admin"// Get the file URL
                withContext(Dispatchers.Main) {
                    onSuccess(fileUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure("Image upload failed: ${e.message}")
                }
            }
        }
    }

    fun uriToFile(context: Context, uri: Uri): JavaFile {
        val file = JavaFile.createTempFile("image", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }
}
