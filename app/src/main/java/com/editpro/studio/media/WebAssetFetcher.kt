package com.editpro.studio.media

import android.content.Context
import android.net.Uri
import com.editpro.studio.model.AssetKind
import com.editpro.studio.model.MediaAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * The agent's "browser": fetch a media file from the web and land it in the project library.
 *
 * Deliberately small. There is no search engine and no scraping — an agent already knows how to
 * find things, and a search API would mean a key, a provider and a rate limit. This takes a direct
 * URL to an image, video or audio file and does the one thing that matters: it puts it in the media
 * library, with the same duration probing, filmstrip and waveform extraction as a hand import, so
 * `add_clip` can use it immediately.
 *
 * Limits: http/https only, 250 MB cap, 60 s read timeout.
 */
object WebAssetFetcher {

    private const val MAX_BYTES = 250L * 1024L * 1024L
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 60_000

    suspend fun fetch(context: Context, url: String, preferredName: String?): MediaAsset? =
        withContext(Dispatchers.IO) {
            val parsed = try {
                URL(url)
            } catch (e: Exception) {
                throw IllegalArgumentException("Not a valid URL: $url")
            }
            require(parsed.protocol == "http" || parsed.protocol == "https") {
                "Only http and https URLs are supported."
            }

            val connection = (parsed.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", "EditProStudio/2.6 (Android)")
                setRequestProperty("Accept", "image/*,video/*,audio/*,*/*")
            }

            try {
                val status = connection.responseCode
                if (status !in 200..299) {
                    throw IllegalStateException("The server returned HTTP $status.")
                }

                val contentType = connection.contentType
                    ?.substringBefore(';')
                    ?.trim()
                    ?.lowercase()
                    .orEmpty()

                val declared = connection.contentLengthLong
                if (declared > MAX_BYTES) {
                    throw IllegalStateException(
                        "That file is ${declared / 1024 / 1024} MB; the limit is 250 MB."
                    )
                }

                val tempDir = File(context.cacheDir, "web").apply { mkdirs() }
                val temp = File(tempDir, buildFileName(preferredName, url, contentType))

                var total = 0L
                connection.inputStream.use { input ->
                    FileOutputStream(temp).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            total += read
                            if (total > MAX_BYTES) {
                                throw IllegalStateException("The download exceeded the 250 MB limit.")
                            }
                            output.write(buffer, 0, read)
                        }
                    }
                }
                if (total == 0L) {
                    temp.delete()
                    throw IllegalStateException("The URL returned an empty file.")
                }

                // Reuse the normal import path so duration, filmstrip and waveform all match a
                // hand import. ContentResolver cannot query a file:// URI, so failures are caught.
                val asset = try {
                    MediaEngine.importMediaUri(context, Uri.fromFile(temp))
                } catch (_: Exception) {
                    null
                }
                temp.delete()
                asset ?: throw IllegalStateException(
                    "Downloaded ${total / 1024} KB but could not read it as media. " +
                        "Check the URL points straight at an image, video or audio file."
                )
            } finally {
                runCatching { connection.disconnect() }
            }
        }

    private fun kindFor(contentType: String, url: String): AssetKind = when {
        contentType.startsWith("image/") -> AssetKind.IMAGE
        contentType.startsWith("audio/") -> AssetKind.AUDIO
        contentType.startsWith("video/") -> AssetKind.VIDEO
        else -> when (extensionOf(url)) {
            "png", "jpg", "jpeg", "webp", "gif", "bmp" -> AssetKind.IMAGE
            "mp3", "wav", "m4a", "aac", "ogg", "flac" -> AssetKind.AUDIO
            else -> AssetKind.VIDEO
        }
    }

    private fun extensionOf(url: String): String =
        url.substringBefore('?').substringBefore('#').substringAfterLast('.', "").lowercase()

    private fun buildFileName(preferred: String?, url: String, contentType: String): String {
        val raw = preferred?.takeIf { it.isNotBlank() }
            ?: url.substringBefore('?').substringBefore('#').substringAfterLast('/')
                .takeIf { it.isNotBlank() }
            ?: "web-asset"
        var name = raw.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_', '.').take(80)
            .ifEmpty { "web-asset" }
        if (!name.contains('.')) name += extensionFor(contentType)
        return name
    }

    private fun extensionFor(contentType: String): String = when (contentType) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        "video/mp4" -> ".mp4"
        "video/quicktime" -> ".mov"
        "video/webm" -> ".webm"
        "audio/mpeg" -> ".mp3"
        "audio/mp4" -> ".m4a"
        "audio/wav", "audio/x-wav" -> ".wav"
        "audio/ogg" -> ".ogg"
        else -> ".bin"
    }
}
