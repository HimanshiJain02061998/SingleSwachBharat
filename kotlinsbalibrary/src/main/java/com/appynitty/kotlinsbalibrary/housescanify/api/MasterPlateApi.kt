package com.appynitty.kotlinsbalibrary.housescanify.api

import com.appynitty.kotlinsbalibrary.housescanify.model.response.HouseIdExistsResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.MasterPlateExist
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface MasterPlateApi {

    @GET("/api/Get/MasterPlateExists")
    suspend fun masterPlateExists(
        @Header("AppId") appId: String,
        @Header("referenceID") referenceId: String
    ): Response<MasterPlateExist>

    @GET("/api/Get/HouseIdExists")
    suspend fun houseIdExists(
        @Header("AppId") appId: String,
        @Header("referenceID") referenceId: String
    ): Response<HouseIdExistsResponse>
}