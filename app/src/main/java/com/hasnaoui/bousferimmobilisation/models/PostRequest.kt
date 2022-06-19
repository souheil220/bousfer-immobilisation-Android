package com.hasnaoui.bousferimmobilisation.models

data class DataAffectation(val text:String,val comment:String,val id:Int,val checked:Boolean)

data class PostRequest(
//    val headers: Map<String, String>,
    val id:Int,
    val comment: String?,
    val quality: String?,
    val asset_id: Int,
    val state:String ="done",
    val inventory_id :Int ,
    val date :String,
    val image1:String,
    val image2:String,
    val image3:String,
    val data:List<DataAffectation>?,
)




