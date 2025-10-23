package com.han.reservation

import android.content.Context
import android.content.SharedPreferences

object PrefHelper {

    private const val PREF_NAME = "user_prefs"
    private const val KEY_UID = "uid"
    private const val KEY_EMAIL = "email"
    private const val KEY_NAME = "name"
    private const val KEY_NICK = "nick"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserInfo(context: Context, uid: String, email: String, name: String, nick: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_UID, uid)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_NICK, nick)
        editor.apply()
    }

    fun getUid(context: Context): String? = getPrefs(context).getString(KEY_UID, null)
    fun getEmail(context: Context): String? = getPrefs(context).getString(KEY_EMAIL, null)
    fun getName(context: Context): String? = getPrefs(context).getString(KEY_NAME, null)
    fun getNick(context: Context): String? = getPrefs(context).getString(KEY_NICK, null)

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}