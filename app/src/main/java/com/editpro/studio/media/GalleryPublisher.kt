package com.editpro.studio.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Copies a finished render into the device's shared Movies collection so it shows up in the
 * Photos / Gallery app.
 *
 * Renders are produced in app-private storage (which the gallery cannot see), so a copy has to
 * be published. Never throws: any failure comes back as a human-readable message so the export
 * itself is never reported as failed because of it.
 */
object GalleryPublisher {

    const val FOLDER = "EditPro"

    fun publish(context: Context, file: File, displayName: String): String {
        if (!file.exists() || file.length() == 0L) return "Saved in the app only (file not found)."
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                publishViaMediaStore(context, file, displayName)
            } else {
                publishLegacy(context, file, displayName)
            }
        } catch (e: SecurityException) {
            "Saved in the app. Photos access denied — use Share to save it yourself."
        } catch (e: Exception) {
            "Saved in the app. Could not add to Photos: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /** Android 10+ — no storage permission required for media the app creates. */
    private fun publishViaMediaStore(context: Context, file: File, displayName: String): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$FOLDER")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri: Uri = resolver.insert(collection, values)
            ?: return "Saved in the app. Could not create a Photos entry."

        try {
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            } ?: throw IllegalStateException("Could not open the Photos entry for writing.")

            val done = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
            return "Added to Photos (Movies/$FOLDER)."
        } catch (e: Exception) {
            // Never leave a half-written entry visible in the gallery.
            runCatching { resolver.delete(uri, null, null) }
            throw e
        }
    }

    /** Android 8–9 — needs WRITE_EXTERNAL_STORAGE; falls back to a clear message if denied. */
    @Suppress("DEPRECATION")
    private fun publishLegacy(context: Context, file: File, displayName: String): String {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), FOLDER)
        if (!dir.exists() && !dir.mkdirs()) return "Saved in the app. Could not create Movies/$FOLDER."
        val target = File(dir, displayName)
        file.inputStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf("video/mp4"), null)
        return "Added to Photos (Movies/$FOLDER)."
    }
}
