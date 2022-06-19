package com.hasnaoui.bousferimmobilisation.models

import com.google.gson.JsonArray

data class SampleModel(val date:String,val id:Int,val name:String,val state:String)

data class TestModel(val inventaire:MutableList<SampleModel>) {
    operator fun get(i: Int): SampleModel {
        return inventaire[i]
    }

    val size: Int
        get() {
            return inventaire.size
        }
}
