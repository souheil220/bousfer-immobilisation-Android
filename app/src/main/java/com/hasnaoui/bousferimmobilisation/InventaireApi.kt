package com.hasnaoui.bousferimmobilisation
import com.hasnaoui.bousferimmobilisation.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface InventaireApi {
    @GET("/get_inventaire")
    suspend fun getInventaire(@Query("loaction")location:String) : Response<TestModel>

    @GET("/get_inventaire_ligne")
    suspend fun getInventaireLine(@Query("inv_id")inv_id:String) : Response<TestModelINV>

    @GET("/get_asset_qr_code")
    suspend fun getInventaireLineQRCode(@Query("qr_code")qr_code:String) : Response<AssetQRModelApi>

    @GET("/get_user_affectated_to")
    suspend fun getUserAffectedTo(@Query("employee_affected_to")employee_affected_to:String) : Response<AffectedToApiModel>

    @GET("/check_list")
    suspend fun getCheckList(@Query("inventory_line_id")inventory_line_id:String) : Response<AffectedToApiModel>


    @POST("/save_asset_asset_line")
    suspend fun saveAssetAssetLine(@Body request:PostRequest):Response<PostRequest>

    @POST("/save_asset_asset_line_exist_not")
    suspend fun saveAssetAssetLineExistNot(@Body request:PostRequest):Response<PostRequest>
}
//