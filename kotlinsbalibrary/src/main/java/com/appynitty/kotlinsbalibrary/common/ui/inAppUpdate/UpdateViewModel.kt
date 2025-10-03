package com.appynitty.kotlinsbalibrary.common.ui.inAppUpdate

import android.app.Application
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val NAME = "mostly-unused"
private const val PI_INSTALL = 3439

@HiltViewModel
class UpdateViewModel @Inject constructor(private val application: Application) : ViewModel(),
    DownloadProgressUpdater.DownloadProgressListener {

    private val installer = application.packageManager.packageInstaller
    private val resolver = application.contentResolver

    fun install(apkUri: Uri) {
        viewModelScope.launch(Dispatchers.Main) {
            installCoroutine(apkUri)
        }
    }

    private suspend fun installCoroutine(apkUri: Uri) =
        withContext(Dispatchers.IO) {

            resolver.openInputStream(apkUri)?.use { apkStream ->
                val length =
                    DocumentFile.fromSingleUri(application, apkUri)?.length() ?: -1
                val params =
                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

                params.setAppPackageName(CommonUtils.PACKAGE_NAME)

                val sessionId = installer.createSession(params)
                val session = installer.openSession(sessionId)

                session.openWrite(NAME, 0, length).use { sessionStream ->
                    apkStream.copyTo(sessionStream)
                    session.fsync(sessionStream)
                }

               val intent = Intent(application.applicationContext, InstallReceiver::class.java)

                val pi = PendingIntent.getBroadcast(
                    application.applicationContext,
                    PI_INSTALL,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                session.commit(pi.intentSender)
                session.close()
            }
        }

    private val updateChannelEvent = Channel<UpdateDialogEvent>()
    val updateEventsFlow = updateChannelEvent.receiveAsFlow()

    private val updateProgressMutableStateFlow =
        MutableStateFlow(UpdateProgress(0, 0))
    val updateProgress = updateProgressMutableStateFlow.asStateFlow()

    fun startDownloadingUpdate(downloadLink: String, downloadManager: DownloadManager) =
        viewModelScope.launch {
            try {

                val downloadUri: Uri = Uri.parse(downloadLink)
                val request = DownloadManager.Request(downloadUri)
                val fileName: String = downloadLink.substring(downloadLink.lastIndexOf("/") + 1)

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        File.separator + fileName
                    )

                val downloadId = downloadManager.enqueue(request)
                updateChannelEvent.send(UpdateDialogEvent.ShowProgressBar)
                val progressUpdater =
                    DownloadProgressUpdater(downloadManager, downloadId, this@UpdateViewModel)
                progressUpdater.run()


            } catch (e: Exception) {
                updateChannelEvent.send(UpdateDialogEvent.ShowErrorMsg(e.message.toString()))
            }
        }

    sealed class UpdateDialogEvent {

        data class ShowErrorMsg(val msg: String) : UpdateDialogEvent()
        object ShowProgressBar : UpdateDialogEvent()
    }

    override fun updateProgress(progress: Long, downloadId: Long) {

        viewModelScope.launch {
            updateProgressMutableStateFlow.emit(UpdateProgress(progress.toInt(), downloadId))
        }

    }


}

data class UpdateProgress(val progress: Int, val downloadId: Long)