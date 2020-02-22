package com.dasbikash.super_activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.dasbikash.android_snackbar_utils.SnackBarUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class SingleFragmentActivity : AppCompatActivity(){

    private val childArgumentStack = Stack<Bundle>()

    private val fragmentTransactionOnGoing = AtomicBoolean(false)

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
    private fun navigateTo(fragment: Fragment) {
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
                SnackBarUtils.showShortSnack(this@SingleFragmentActivity,waitMessage)
                return@launch
            }
            fragmentTransactionOnGoing.getAndSet(true)
            runOnMainThread({
                runIfActive {
                    fragmentTransaction.commit()
                }
            })
        }
    }

    fun addFragment(fragment: Fragment,clearFragmentStack:Boolean = false) {
        if (clearFragmentStack){
            clearFragmentStack()
        }else{
            supportFragmentManager.findFragmentById(getLoneFrameId())?.apply {
                val bundle:Bundle
                if (this.arguments !=null){
                    bundle = this.arguments!!
                }else{
                    bundle = Bundle()
                }
                addTypeTag(bundle, this)
                childArgumentStack.push(bundle)
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
            do {
                val arguments = childArgumentStack.pop()
                getInstance<Fragment>(arguments)?.let {
                    navigateTo(it)
                    return
                }
            }while (childArgumentStack.isNotEmpty())
            super.onBackPressed()
        }else {
            super.onBackPressed()
        }
    }

    private var waitMessage = "Please wait..."

    protected fun setWaitMessage(waitMessage:String){
        if (waitMessage.isNotBlank()) {
            this.waitMessage = waitMessage.trim()
        }
    }

    @IdRes
    protected abstract fun getLoneFrameId():Int
    @LayoutRes
    protected abstract fun getLayoutID(): Int
    protected abstract fun getDefaultFragment():Fragment

    companion object{

        private const val ARG_FRAGMENT_TYPE =
            "com.dasbikash.super_activity.SingleFragmentActivity.ARG_FRAGMENT_TYPE"

        private fun <T:Fragment> addTypeTag(arguments:Bundle, fragment: T){
            arguments.putSerializable(ARG_FRAGMENT_TYPE,fragment.javaClass.canonicalName!!)
        }

        private fun <T:Fragment> getInstance(bundle: Bundle):T?{
            try {
                val typeString = bundle.getSerializable(ARG_FRAGMENT_TYPE) as String
                val fragment = Class.forName(typeString).newInstance() as T
                fragment.arguments = bundle
                return fragment
            }catch (ex:Throwable){
                ex.printStackTrace()
                return null
            }
        }
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