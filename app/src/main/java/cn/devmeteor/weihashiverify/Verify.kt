package cn.devmeteor.weihashiverify

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Verify {
    @GET("/v2/device/verify")
    fun verify(@Query("deviceId") deviceId:String,@Query("hostName")hostName:String):Call<Response>
}