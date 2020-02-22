package com.dasbikash.super_activity

import android.os.Bundle
import androidx.fragment.app.Fragment


abstract class ChildFragment : Fragment() {

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
