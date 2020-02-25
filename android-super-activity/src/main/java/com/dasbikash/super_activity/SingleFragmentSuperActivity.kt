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
 * ```
 *
 * Abstract super activity to manage stack of all child fragments for a single activity application.
 * ```
 * Descendant activity will have to register a [FrameLayout](https://developer.android.com/reference/android/widget/FrameLayout)
 * which 'SingleFragmentSuperActivity' will use to host child fragments.
 * ```
 * ```
 * ##### Fragments could be loaded in any one of two ways:
 * * Stacked one after another.
 * * Clearing back stack.
 * ```
 * If back stack is not cleared then 'SingleFragmentSuperActivity' will stack arguments (not fragment) of loaded fragments.
 * On back press it will pop fragment arguments in descending order; then will create and load fragment until stack is empty.
 *
 * ```
 *
 * ### Usage requirements/procedure:
 * * Single activity of user application will have to extend this class.
 * * Child activity will have to register default fragment via 'getDefaultFragment()' protected method.
 * * Child activity will have to pass it's layout id on 'getLayoutID()' protected method.
 * * Child activity will have to pass the frame id on 'getLoneFrameId()' protected method on which 'SingleFragmentSuperActivity' will load fragments.
 * ```
 *
 * @author Bikash Das(das.bikash.dev@gmail.com)
 * */
abstract class SingleFragmentSuperActivity : AppCompatActivity(){

    private val childFragmentArgumentStack = Stack<Bundle>()

    private val fragmentTransactionOnGoing = AtomicBoolean(false)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
    }

    @CallSuper
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

    /**
     * Method to load fragment on registered frame.
     *
     * @param fragment Fragment to be loaded.
     * @param clearFragmentStack whether fragment back stack should be cleared
     * @param doOnFragmentLoad Optional functional parameter that will run after fragment loading
     * */
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

    /**
     * Method to manually load fragment from back-stack(if available).
     *
     * @return 'true' if fragment found and loaded from back-stack
     * */
    protected fun loadFragmentFromBackStack():Boolean{
        val arguments = childFragmentArgumentStack.pop()
        getInstance<Fragment>(arguments)?.let {
            navigateTo(it)
            return true
        }
        return false
    }

    /**
     * Method to manually clear fragment back-stack.
     *
     * @return 'true' if fragment found and loaded from back-stack
     * */
    protected fun clearFragmentBackStack() {
        while (!childFragmentArgumentStack.empty()) {
            childFragmentArgumentStack.pop()
        }
    }

    private var waitMessage = "Please wait..."

    /**
     * Method to set fragment loader busy message.
     *
     * @return 'true' if fragment found and loaded from back-stack
     * */
    protected fun setWaitMessage(waitMessage:String){
        this.waitMessage = waitMessage.trim()
    }

    //Frame id that will host fragment
    @IdRes
    protected abstract fun getLoneFrameId():Int

    //Child activity layout Id
    @LayoutRes
    protected abstract fun getLayoutID(): Int

    //Default fragment
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