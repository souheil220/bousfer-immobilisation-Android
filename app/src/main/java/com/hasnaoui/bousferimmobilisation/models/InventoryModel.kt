package com.hasnaoui.bousferimmobilisation.models

data class InventoryModel(
    val asset_id:ArrayList<Any>,
    val comment: String,
    val data: Data,
    val date:String,
    val id:Int,
    val quality:String,
    val state:String,
    val image:Byte?,
    val image2: String?,
    val image3: String?
)

data class CentreCoutModel(val id: Int, val name: String)

data class Data(
    val category_id: List<Any>,
    val centre_cout_id: List<CentreCoutModel>,
    val centre_de_cout: String,
    val code: String,
    val employee_affected_id:List<Any>,
    val id: Int,
    val location: String,
    val name: String,
    val num_serie: String,
    val quantite:Double
)

data class TestModelINV(val inv_line: MutableList<InventoryModel>){
    operator fun get(i: Int): InventoryModel {
        return inv_line[i]
    }
    val size: Int
        get() {
            return inv_line.size
        }
}

