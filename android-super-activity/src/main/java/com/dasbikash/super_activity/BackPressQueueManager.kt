package com.dasbikash.super_activity

interface BackPressQueueManager {
    fun addToBackPressTaskQueue(task:()->Unit):String
    fun removeTaskFromQueue(taskTag:String)
}