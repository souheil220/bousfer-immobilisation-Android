package com.hasnaoui.bousferimmobilisation.utils

object Constants {
    const val ALL_OPTIONS :String= "Tous"
    const val FILTER_SELECTION:String = "FilterSelection"
    fun inventoryOption():ArrayList<String>{
        val list = ArrayList<String>()
        list.add("Inventorié")
        list.add("Non Inventorié")

        return list
    }
}