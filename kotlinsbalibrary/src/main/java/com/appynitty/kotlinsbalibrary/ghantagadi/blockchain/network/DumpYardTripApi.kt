package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.network

import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DumpYardTripApi {

    @POST("api/Save/DumpyardTrip")
    suspend fun syncDumpYardTrip(
        @Header("AppId") appId: String,
        @Body tripRequest: List<TripRequest>
    ): Response<List<TripResponse>>

}