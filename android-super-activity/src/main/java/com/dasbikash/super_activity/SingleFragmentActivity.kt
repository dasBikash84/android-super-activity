package com.dasbikash.super_activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import java.util.*

abstract class SingleFragmentActivity : AppCompatActivity(){

    private val childArgumentStack = Stack<Bundle>()

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
    private fun navigateTo(fragment: ChildFragment)
            = supportFragmentManager.beginTransaction().replace(getLoneFrameId(), fragment).commit()

    fun addFragment(fragment: ChildFragment,clearFragmentStack:Boolean = false) {
        if (clearFragmentStack){
            clearFragmentStack()
        }else{
            supportFragmentManager.findFragmentById(getLoneFrameId())?.apply {
                if (this is ChildFragment){
                    ChildFragment.addTypeTag(this.arguments!!,this)
                    childArgumentStack.push(this.arguments!!)
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
    abstract fun getDefaultFragment():ChildFragment

}
