package com.dasbikash.super_activity

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import java.lang.IllegalStateException

abstract class ChildFragment : Fragment() {

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity as SingleFragmentActivity
        }catch (ex:Throwable){
            throw IllegalStateException()
        }
    }

    protected fun addFragment(fragment: ChildFragment, clearFragmentStack:Boolean = false) {
        (activity as SingleFragmentActivity).addFragment(fragment, clearFragmentStack)
    }

    companion object {

        private const val ARG_FRAGMENT_TYPE =
            "com.dasbikash.super_activity.ChildFragment.ARG_FRAGMENT_TYPE"

        class Builder <T:ChildFragment> internal constructor(private val type:Class<T>){
            private var instanceBuildStatus = false
            private val arguments:Bundle = Bundle()

            fun getArguments():Bundle?{
                if (!instanceBuildStatus) {
                    return arguments
                }
                return null
            }

            fun build():T{
                instanceBuildStatus = true
                val fragment = type.newInstance()
                arguments.putSerializable(ARG_FRAGMENT_TYPE,fragment.javaClass.canonicalName!!)
                fragment.arguments = this.arguments
                return fragment
            }
        }

        internal fun <T:ChildFragment> addTypeTag(arguments:Bundle, fragment: T){
            arguments.putSerializable(ARG_FRAGMENT_TYPE,fragment.javaClass.canonicalName!!)
        }

        internal fun getInstance(bundle: Bundle):ChildFragment{
            val typeString = bundle.getSerializable(ARG_FRAGMENT_TYPE) as String
            val fragment = Class.forName(typeString).newInstance() as ChildFragment
            fragment.arguments = bundle
            return fragment
        }

        @JvmStatic
        fun <T:ChildFragment> getBuilder(type:Class<T>) = Builder(type)
    }
}
