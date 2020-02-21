package com.dasbikash.super_activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.*

abstract class ActivitySingleFragment : AppCompatActivity(),BackPressQueueManager {

    abstract fun getDefaultFragment():Fragment

    private val backPressTaskMap = mutableMapOf<String,()->Unit>()
    private val backPressTaskTagList = mutableListOf<String>()

    override fun addToBackPressTaskQueue(task: () -> Unit): String {
        val uuid= UUID.randomUUID().toString()
        backPressTaskMap.put(uuid,task)
        backPressTaskTagList.add(uuid)
        return uuid
    }

    override fun removeTaskFromQueue(taskTag: String) {
        val task = backPressTaskMap.get(taskTag)
        task?.let {
            backPressTaskTagList.remove(taskTag)
            backPressTaskMap.remove(taskTag)
        }
    }

    override fun onBackPressed() {
        if (backPressTaskTagList.isNotEmpty()){
            val taskTag = backPressTaskTagList.last()
            val task = backPressTaskMap.get(taskTag)!!
            task()
            backPressTaskTagList.remove(taskTag)
            backPressTaskMap.remove(taskTag)
        }else {
            super.onBackPressed()
        }
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        loadDefaultFragment()
    }

    @LayoutRes
    protected abstract fun getLayoutID(): Int


    private fun loadDefaultFragment(){
        supportFragmentManager.findFragmentById(getLoneFrameId()).apply {
            if (this == null || this.javaClass != getDefaultFragment().javaClass){
                navigateTo(getDefaultFragment())
            }
        }
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    private fun navigateTo(fragment: Fragment, addToBackstack: Boolean=false) {
        val transaction =
                    supportFragmentManager.beginTransaction().replace(getLoneFrameId(), fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    private fun addFragment(fragment: Fragment) {
        val transaction =
                    supportFragmentManager.beginTransaction().add(getLoneFrameId(), fragment)
        transaction.commit()
    }

    private fun removeFragment(fragment: Fragment) {
        val transaction =
                    supportFragmentManager.beginTransaction().remove(fragment)
        transaction.commit()
    }

    @IdRes
    protected abstract fun getLoneFrameId():Int

}
