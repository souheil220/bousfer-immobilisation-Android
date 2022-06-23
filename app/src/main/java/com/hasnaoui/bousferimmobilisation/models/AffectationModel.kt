package com.hasnaoui.bousferimmobilisation.models

data class AffectationModel(var checked:Any, var comment:Any, val id:Int, val name:String)


data class AffectedToApiModel(val response:MutableList<AffectationModel>){
    operator fun get(i: Int): AffectationModel {
        return response[i]
    }
    val size: Int
        get() {
            return response.size
        }
}