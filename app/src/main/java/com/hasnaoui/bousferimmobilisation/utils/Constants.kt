package com.hasnaoui.bousferimmobilisation.utils

object Constants {
    const val ALL_OPTIONS :String= "Tous"
    const val FILTER_SELECTION:String = "FilterSelection"
//    const val BASE_URL = "http://10.0.2.2:5000"
    const val BASE_URL = "http://192.168.137.250:5000"
    fun inventoryOption():ArrayList<String>{
        val list = ArrayList<String>()
        list.add("Inventorié")
        list.add("Non Inventorié")

        return list
    }
}