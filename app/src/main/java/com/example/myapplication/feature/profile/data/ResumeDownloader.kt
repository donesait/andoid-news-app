package com.example.myapplication.feature.profile.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class ResumeDownloader(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun downloadAndOpen(url: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        if (url.isBlank()) {
            cont.resume(Result.failure(IllegalArgumentException("Пустая ссылка на резюме")))
            return@suspendCancellableCoroutine
        }
        val uri = runCatching { Uri.parse(url) }.getOrElse {
            cont.resume(Result.failure(it))
            return@suspendCancellableCoroutine
        }
        val fileName = uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: "resume.pdf"
        val request = DownloadManager.Request(uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Скачивание резюме")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val downloadId = downloadManager.enqueue(request)
        var receiverRegistered = true
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val completedId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
                if (completedId != downloadId) return
                if (receiverRegistered) {
                    context.unregisterReceiver(this)
                    receiverRegistered = false
                }
                val query = DownloadManager.Query().setFilterById(downloadId)
                downloadManager.query(query).use { cursor ->
                    if (!cursor.moveToFirst()) {
                        cont.resume(Result.failure(IllegalStateException("Не удалось скачать файл")))
                        return
                    }
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status != DownloadManager.STATUS_SUCCESSFUL) {
                        cont.resume(Result.failure(IllegalStateException("Скачивание завершилось ошибкой")))
                        return
                    }
                }
                val fileUri = downloadManager.getUriForDownloadedFile(downloadId)
                    ?: cont.resume(Result.failure(IllegalStateException("Не удалось получить файл"))).let { return }
                val mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId) ?: "application/pdf"
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching {
                    context.startActivity(openIntent)
                }.onSuccess {
                    cont.resume(Result.success(Unit))
                }.onFailure { error ->
                    cont.resume(Result.failure(error))
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        cont.invokeOnCancellation {
            if (receiverRegistered) {
                context.unregisterReceiver(receiver)
                receiverRegistered = false
            }
            downloadManager.remove(downloadId)
        }
    }
}

