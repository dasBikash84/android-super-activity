package com.dasbikash.super_activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.dasbikash.android_snackbar_utils.SnackBarUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class SingleFragmentActivity : AppCompatActivity(){

    private val childArgumentStack = Stack<Bundle>()

    private var fragmentTransactionOnGoing = AtomicBoolean(false)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        loadDefaultFragment()
    }

    private fun loadDefaultFragment(){
        supportFragmentManager.findFragmentById(getLoneFrameId()).apply {
            if (this == null || this.javaClass != getDefaultFragment().javaClass){
                addFragment(getDefaultFragment())
            }
        }
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    private fun navigateTo(fragment: ChildFragment) {
        val fragmentTransaction =
            supportFragmentManager
            .beginTransaction()
            .replace(getLoneFrameId(), fragment)
            .runOnCommit {
                GlobalScope.launch {
                    fragmentTransactionOnGoing.getAndSet(false)
                }
            }
        GlobalScope.launch {
            if (fragmentTransactionOnGoing.get()){
                SnackBarUtils.showShortSnack(this@SingleFragmentActivity,WAIT_MESSAGE)
                return@launch
            }
            fragmentTransactionOnGoing.getAndSet(true)
            runOnUiThread {
                fragmentTransaction.commit()
            }
        }
    }

    fun addFragment(fragment: ChildFragment,clearFragmentStack:Boolean = false) {
        if (clearFragmentStack){
            clearFragmentStack()
        }else{
            supportFragmentManager.findFragmentById(getLoneFrameId())?.apply {
                if (this is ChildFragment){
                    val bundle:Bundle
                    if (this.arguments !=null){
                        bundle = this.arguments!!
                    }else{
                        bundle = Bundle()
                    }
                    ChildFragment.addTypeTag(bundle, this)
                    childArgumentStack.push(bundle)
                }
            }
        }
        navigateTo(fragment)
    }

    private fun clearFragmentStack() {
        while (!childArgumentStack.empty()) {
            childArgumentStack.pop()
        }
    }

    @CallSuper
    override fun onBackPressed() {
        if (childArgumentStack.isNotEmpty()){
            val arguments = childArgumentStack.pop()
            navigateTo(ChildFragment.getInstance(arguments))
        }else {
            super.onBackPressed()
        }
    }

    @IdRes
    protected abstract fun getLoneFrameId():Int
    @LayoutRes
    protected abstract fun getLayoutID(): Int
    protected abstract fun getDefaultFragment():ChildFragment

    companion object{
        private const val WAIT_MESSAGE = "Please wait..."
    }
}

internal fun AppCompatActivity.runIfActive(task:()->Any?){
    if (lifecycle.currentState == Lifecycle.State.RESUMED) {
        task()
    }
}

internal fun runOnMainThread(task: () -> Any?,delayMs:Long=0L){
    Handler(Looper.getMainLooper()).postDelayed( { task() },delayMs)
}