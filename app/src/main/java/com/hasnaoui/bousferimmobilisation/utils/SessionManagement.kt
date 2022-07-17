package com.hasnaoui.bousferimmobilisation.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManagement {
    private  var sharedPreferences: SharedPreferences
    private  var editor: SharedPreferences.Editor


    constructor(context: Context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    companion object{
        val SHARED_PREF_NAME = "Login_Preference"
        val IS_LOGIN = "isLoggedIn"
        val KEY_ID = "id"
    }

    fun saveSession(id:Int){
        editor.putBoolean(IS_LOGIN,true)
        editor.putInt(KEY_ID,id)
        editor.commit()
    }

    fun logOutUser(){
        editor.clear()
        editor.commit()
    }

    fun getSession():Boolean{
        return sharedPreferences.getBoolean(IS_LOGIN,false)
    }

    fun getIDFromSession():Int{
        Log.e("id session",sharedPreferences.getInt(KEY_ID,0).toString())
        return sharedPreferences.getInt(KEY_ID,0)
    }

}