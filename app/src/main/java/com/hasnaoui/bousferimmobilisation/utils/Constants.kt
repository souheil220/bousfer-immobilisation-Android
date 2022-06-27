package com.hasnaoui.bousferimmobilisation.utils

object Constants {
    const val ALL_OPTIONS: String = "Tous"
    const val FILTER_SELECTION: String = "FilterSelection"
    const val EXIST: String = "exist"
    const val FROM: String = "from"
    const val CODE: String = "code"
    const val INVENTORY_TITLE: String = "inv_title"
    const val ASSET_ID: String = "asset_id"
    const val INVENTORY_ID: String = "inv_id"
    const val INVENTORY_LINE_ID: String = "inventory_line_id"
    const val QUANTITY: String = "quantite"
    const val CATEGORY: String = "category"
    const val NAME: String = "name"
    const val CENTRE_DE_COUT: String = "centre_de_cout"
    const val LOCATION: String = "location"
    const val NUMERO_DE_SERIE: String = "numSerie"
    const val QUALITY: String = "quality"
    const val DATE_INVENTORY: String = "dateInventory"
    const val ETAT: String = "etat"
    const val COMMENT: String = "comment"
    const val IMAGE: String = "image"
    const val EMPLOYEE_AFFECTED_TO_ID: String = "employee_affected_id"
    const val EMPLOYEE_AFFECTED_TO_NAME: String = "employee_affected_id_name"


    //    const val BASE_URL = "http://10.0.2.2:5000"
//    const val BASE_URL = "http://192.168.137.1:5000"
    const val BASE_URL = "http://10.1.12.47:5000"
    fun inventoryOption(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("Inventorié")
        list.add("Non Inventorié")

        return list
    }
}