package com.shagworld.notes.uitls

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import javax.crypto.SecretKey

object Pref {
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private const val PREF_NAME = "PREF_NAME"

    private const val USER_LOGGED_IN = "USER_LOGGED_IN"
    private const val AES_SECRET_KEY = "AES_SECRET_KEY"

    private const val NOTES_LIST = "NOTES_LIST"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var userLoggedIn: Boolean
        get() = preferences.getBoolean(USER_LOGGED_IN, false)
        set(value) = preferences.edit { it.putBoolean(USER_LOGGED_IN, value) }

    var secretAESKey: String
        get() = preferences.getString(AES_SECRET_KEY, "").toString()
        set(value) = preferences.edit { it.putString(AES_SECRET_KEY, value) }

   /* var AESKey: SecretKey?
        get() {
            val json = preferences.getString(AES_SECRET_KEY, null)
            return json?.let { Gson().fromJson(it, SecretKey::class.java) }
        }
        set(value) {
            val json = Gson().toJson(value)
            preferences.edit().putString(AES_SECRET_KEY, json).apply()
        }*/


//    var notes: ArrayList<Note>
//        get() {
//            val jsonString = preferences.getString(NOTES_LIST, null) ?: return arrayListOf()
//            return try {
//                val array = Gson().fromJson(jsonString, Array<Note>::class.java)
//                array?.toList()?.let { ArrayList(it) } ?: arrayListOf()
//            } catch (e: Exception) {
//                arrayListOf()
//            }
//        }
//        set(value) = preferences.edit {
//            it.putString(NOTES_LIST, Gson().toJson(value))
//        }


}