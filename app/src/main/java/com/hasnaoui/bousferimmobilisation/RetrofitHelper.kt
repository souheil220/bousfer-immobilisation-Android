package com.hasnaoui.bousferimmobilisation

import com.hasnaoui.bousferimmobilisation.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


object RetrofitHelper {


    fun getInstance(): Retrofit {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(Constants.BASE_URL    )
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}