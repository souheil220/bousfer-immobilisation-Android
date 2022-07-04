package com.hasnaoui.bousferimmobilisation.models



data class AssetQRModel(
    val affectation_id: List<Any>,
    val category_id: List<Any>,
    val center_cout_id: List<Any>,
    val centre_de_cout: String,
    val code:String,
    val employee_affected_id:List<Any>,
    val id:Int,
    val location:String,
    val name:String,
    val num_serie:String,
    val quantity:Double
)

data class AssetQRModelApi(val asset: MutableList<AssetQRModel>){
    operator fun get(i: Int): AssetQRModel {
        return asset[i]
    }
    val size: Int
        get() {
            return asset.size
        }
}
