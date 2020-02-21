package com.dasbikash.super_activity

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal fun runOnMainThread(task: () -> Any?,delayMs:Long=0L){
    Handler(Looper.getMainLooper()).postDelayed( { task() },delayMs)
}


internal fun ByteArray.toCharArray():CharArray{
    val charArray = CharArray(this.size)
    for (i in 0..size-1){
        charArray.set(i,get(i).toChar())
    }
    return charArray
}

internal fun CharArray.byteArray():ByteArray{
    val bytes = ByteArray(this.size)
    for (i in 0..size-1){
        bytes.set(i,get(i).toByte())
    }
    return bytes
}

internal fun ByteArray.toSerializedString():String = String(toCharArray())
internal fun String.deserialize():ByteArray = toCharArray().byteArray()

internal fun java.io.Serializable.toByteArray():ByteArray{
    val buffer = ByteArrayOutputStream()
    val oos = ObjectOutputStream(buffer)
    oos.writeObject(this)
    oos.close()
    return buffer.toByteArray()
}

internal fun <T:java.io.Serializable> ByteArray.toSerializable(type:Class<T>):T{
    return ObjectInputStream(ByteArrayInputStream(this)).readObject() as T
}

internal suspend fun <T:Any> runSuspended(task:()->T):T {
    coroutineContext().let {
        return withContext(it) {
            return@withContext async(Dispatchers.IO) { task() }.await()
        }
    }
}

internal suspend fun coroutineContext(): CoroutineContext = suspendCoroutine { it.resume(it.context) }