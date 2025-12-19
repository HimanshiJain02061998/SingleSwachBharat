package com.appynitty.kotlinsbalibrary.common.backgroundTask

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import javax.inject.Inject

class SyncWorker @Inject constructor(
    appContext: Context,
    workerParams: WorkerParameters,
    val backgroundTaskHelper:BackgroundTaskHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            backgroundTaskHelper.submitOfflineData()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}