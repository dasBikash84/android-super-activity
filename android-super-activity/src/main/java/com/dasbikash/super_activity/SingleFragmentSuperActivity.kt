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

/**
 * Abstract super activity to hold and manage stack of all child fragments
 * for a single activity application. If child activity will
 *
 * Usage requirements/procedure:
 *
 * Single activity of user application will have to extend this class.
 * Child activity will have to pass it's layout id on 'getLayoutID()' protected method.
 * Child activity will have to pass the id of fragment housing frame id on 'getLoneFrameId()' protected method.
 *
 *
 * Implementation example: https://github.com/dasBikash84/super_activity_example
 * */
abstract class SingleFragmentSuperActivity : AppCompatActivity(){

    private val childFragmentArgumentStack = Stack<Bundle>()

    private val fragmentTransactionOnGoing = AtomicBoolean(false)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
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
     * Trigger a navigation to the specified fragment,
     * optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    private fun navigateTo(fragment: Fragment,
                           doOnFragmentLoad:(()->Any?)?=null) {
        val fragmentTransaction =
            supportFragmentManager
            .beginTransaction()
            .replace(getLoneFrameId(), fragment)
            .runOnCommit {
                GlobalScope.launch {
                    fragmentTransactionOnGoing.getAndSet(false)
                }
                runOnMainThread({doOnFragmentLoad?.invoke()})
            }
        GlobalScope.launch {
            if (fragmentTransactionOnGoing.get()){
                if (waitMessage.isNotBlank()) {
                    SnackBarUtils.showShortSnack(this@SingleFragmentSuperActivity, waitMessage)
                }
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

    fun addFragment(fragment: Fragment,
                    clearFragmentStack:Boolean = false,
                    doOnFragmentLoad:(()->Any?)?=null) {
        if (clearFragmentStack){
            clearFragmentBackStack()
        }else{
            supportFragmentManager.findFragmentById(getLoneFrameId())?.apply {
                val bundle:Bundle
                if (this.arguments !=null){
                    bundle = this.arguments!!
                }else{
                    bundle = Bundle()
                }
                addTypeTag(bundle, this)
                childFragmentArgumentStack.push(bundle)
            }
        }
        navigateTo(fragment,doOnFragmentLoad)
    }

    @CallSuper
    override fun onBackPressed() {
        if (childFragmentArgumentStack.isNotEmpty()){
            do {
                if (loadFragmentFromBackStack()){
                    return
                }
            }while (childFragmentArgumentStack.isNotEmpty())
            super.onBackPressed()
        }else {
            super.onBackPressed()
        }
    }

    protected fun loadFragmentFromBackStack():Boolean{
        val arguments = childFragmentArgumentStack.pop()
        getInstance<Fragment>(arguments)?.let {
            navigateTo(it)
            return true
        }
        return false
    }

    protected fun clearFragmentBackStack() {
        while (!childFragmentArgumentStack.empty()) {
            childFragmentArgumentStack.pop()
        }
    }

    private var waitMessage = "Please wait..."

    protected fun setWaitMessage(waitMessage:String){
        this.waitMessage = waitMessage.trim()
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