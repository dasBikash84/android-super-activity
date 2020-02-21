package com.dasbikash.super_activity

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * Helper class for Shared Preference related operations.
 * Any <b>Serializable</b> object including
 * object of Long, Int, Float, Boolean, String types
 * can be saved on shared preference
 *
 * @author Bikash Das(das.bikash.dev@gmail.com)
 * */
internal class SharedPreferenceUtils(private val SP_FILE_KEY:String){

    /**
     * Method to get hold of subject 'SharedPreferences' instance
     *
     * @param context Android Context
     * @return returns instance of subject 'SharedPreferences'
     * */
    fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SP_FILE_KEY, Context.MODE_PRIVATE)

    /**
     * Method to get hold of subject 'SharedPreferences.Editor' instance
     *
     * @param context Android Context
     * @return returns instance of subject 'SharedPreferences.Editor'
     * */
    fun getSpEditor(context: Context): SharedPreferences.Editor =
        getSharedPreferences(context).edit()

    /**
     * Method to save(blocking) object on Shared Preference
     *
     * @param context Android Context
     * @param data subject Serializable object that is to be saved
     * @param key unique key to the object to be saved
     * */
    fun saveDataSync(context: Context, data: Serializable, key: String):Boolean {
        return saveData(getSpEditor(context),data, key)
    }

    /**
     * Method to save(async) object on Shared Preference
     *
     * @param context Android Context
     * @param data subject Serializable object that is to be saved
     * @param key unique key to the object to be saved
     * */
    fun saveData(context: Context, data: Serializable, key: String) {
        GlobalScope.launch(Dispatchers.IO) {
            saveData(getSpEditor(context),data, key)
        }
    }

    /**
     * Method to save(suspended) object on Shared Preference
     *
     * @param context Android Context
     * @param data subject Serializable object that is to be saved
     * @param key unique key to the object to be saved
     * */
    suspend fun saveDataSuspended(context: Context, data: Serializable, key: String):Boolean {
        return runSuspended {
            saveData(getSpEditor(context),data, key)
        }
    }

    private fun saveData(editor: SharedPreferences.Editor,data: Serializable, key: String):Boolean{
        when (data) {
            is Long     -> editor.putLong(key, data as Long)
            is Int      -> editor.putInt(key, data as Int)
            is Float    -> editor.putFloat(key, data as Float)
            is Boolean  -> editor.putBoolean(key, data as Boolean)
            is String  -> editor.putString(key, data.toString())
            else  -> editor.putString(key,data.toByteArray().toSerializedString())
        }
        editor.apply()
        return true
    }

    /**
     * Method to read serializable object from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param exampleObj example object of subject type
     * */
//    fun <T : Serializable> getData(context: Context, key: String,exampleObj:T): T? =
//        getData(context,key,exampleObj.javaClass)

    /**
     * Method to read serializable object from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param type subject class type
     * */
    fun <T : Serializable> getData(context: Context, key: String,type:Class<T>): T? {
        var retVal:T? = null
        getSharedPreferences(context).let {
            if (it.contains(key)){
                try {
                    retVal =  when {
                        type.isAssignableFrom(Long::class.java) -> it.getLong(key, Long.MIN_VALUE)
                        type.isAssignableFrom(Int::class.java) -> it.getInt(key, Int.MIN_VALUE)
                        type.isAssignableFrom(Float::class.java) -> it.getFloat(key, Float.MIN_VALUE)
                        type.isAssignableFrom(Boolean::class.java) -> it.getBoolean(key, false)
                        type.isAssignableFrom(String::class.java) -> it.getString(key, "")
                        else -> it.getString(key,"")!!.deserialize().toSerializable(type)
                    } as T?
                }catch (ex:Throwable){
                    ex.printStackTrace()
                }
            }else{
                retVal = null
            }
        }
        return retVal
    }

    /**
     * Removes object with given key from Shared Preferences
     *
     * @param context Android Context
     * @param key unique key to the saved object
     * */
    fun removeKey(context: Context,key: String)
            = getSpEditor(context).remove(key).apply()

    /**
     * Checks whwather object with given key exists on Shared Preferences
     *
     * @param context Android Context
     * @param key unique key to the saved object
     * @return true if found else false
     * */
    fun checkIfExists(context: Context, key: String):Boolean
            = getSharedPreferences(context).contains(key)

    /**
     * Clears all saved data from subject Shared Preferences
     *
     * @param context Android Context
     * */
    fun clearAll(context: Context):Boolean = getSpEditor(context).clear().commit()

    /**
     * Registers Shared Preference Change Listener     *
     *
     * */
    fun registerOnChangeListener(context: Context,
                                 listener: SharedPreferences.OnSharedPreferenceChangeListener)
            = getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener)

    /**
     * Un-registers Shared Preference Change Listener     *
     *
     * */
    fun unRegisterOnChangeListener(context: Context,
                                   listener: SharedPreferences.OnSharedPreferenceChangeListener)
            = getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener)

    companion object{

        private val DEFAULT_SP_FILE_KEY:String =
            "com.dasbikash.super_activity.SharedPreferenceUtils.SP_FILE_KEY"

        /**
         * Returns class instance for given Shared Preferences storage file
         *
         * @param SP_FILE_KEY Shared Preferences storage file name
         * @return instance of SharedPreferenceUtils
         * */
        @JvmStatic
        fun getInstance(SP_FILE_KEY:String = DEFAULT_SP_FILE_KEY) = SharedPreferenceUtils(SP_FILE_KEY)
    }
}
