package com.dasbikash.super_activity

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal fun runOnMainThread(task: () -> Any?,delayMs:Long=0L){
    Handler(Looper.getMainLooper()).postDelayed( { task() },delayMs)
}

internal suspend fun <T:Any> runSuspended(task:()->T):T {
    coroutineContext().let {
        return withContext(it) {
            return@withContext async(Dispatchers.IO) { task() }.await()
        }
    }
}

internal suspend fun coroutineContext(): CoroutineContext = suspendCoroutine { it.resume(it.context) }