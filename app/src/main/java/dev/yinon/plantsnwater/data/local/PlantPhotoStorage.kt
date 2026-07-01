package dev.yinon.plantsnwater.data.local

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlantPhotoStorage(
    private val context: Context,
    private val clock: Clock = Clock.systemUTC()
) {
    private val photoDir: File
        get() = File(context.filesDir, PHOTO_DIR).also { it.mkdirs() }

    suspend fun copyFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val extension = context.contentResolver.getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        val file = newPhotoFile(extension)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Selected image could not be opened." }
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.name
    }

    suspend fun createCameraCapture(): PendingPhotoCapture = withContext(Dispatchers.IO) {
        val file = newPhotoFile("jpg")
        PendingPhotoCapture(
            localReference = file.name,
            contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        )
    }

    fun displayUri(localReference: String): Uri = File(photoDir, localReference).toUri()

    suspend fun delete(localReference: String) = withContext(Dispatchers.IO) {
        File(photoDir, localReference).delete()
    }

    private fun newPhotoFile(extension: String): File {
        val safeExtension = extension.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "jpg" }
        val timestamp = clock.instant().toEpochMilli()
        return File(photoDir, "$timestamp-${UUID.randomUUID()}.$safeExtension")
    }

    private companion object {
        const val PHOTO_DIR = "plant-photos"
    }
}

data class PendingPhotoCapture(
    val localReference: String,
    val contentUri: Uri
)
