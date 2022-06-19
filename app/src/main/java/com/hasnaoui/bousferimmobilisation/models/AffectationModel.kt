package com.hasnaoui.bousferimmobilisation.models

data class AffectationModel(var checked:Any, var comment:Any, val id:Int, val name:String)

data class AffectedToModel(val firstname:String,val id:Int,val name_related:String,val resource_id:List<Any>,val societe:String)

data class AffectedToApiModel(val response:MutableList<AffectationModel>){
    operator fun get(i: Int): AffectationModel {
        return response[i]
    }
    val size: Int
        get() {
            return response.size
        }
}