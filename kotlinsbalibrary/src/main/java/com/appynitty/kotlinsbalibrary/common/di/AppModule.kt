package com.appynitty.kotlinsbalibrary.common.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.appynitty.kotlinsbalibrary.BuildConfig.BASE_URL
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.api.*
import com.appynitty.kotlinsbalibrary.common.database.SbaDatabase
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.api.DutyApi
import com.appynitty.kotlinsbalibrary.ghantagadi.api.EmployeeApiService
import com.appynitty.kotlinsbalibrary.ghantagadi.api.ScanQrApi
import com.appynitty.kotlinsbalibrary.ghantagadi.api.WorkHistoryApi
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.network.DumpYardTripApi
import com.appynitty.kotlinsbalibrary.housescanify.api.EmpDutyApi
import com.appynitty.kotlinsbalibrary.housescanify.api.EmpGcApi
import com.appynitty.kotlinsbalibrary.housescanify.api.EmpWorkHistoryApi
import com.appynitty.kotlinsbalibrary.housescanify.api.MasterPlateApi
import com.appynitty.kotlinsbalibrary.housescanify.api.PropertyTypeApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * CREATED BY SANATH GOSAVI 13-02-2023
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    /**
     * ROOM DATABASE INSTANCE
     */
    @Provides
    @Singleton
    fun provideDatabase(app: Application): SbaDatabase =
        Room.databaseBuilder(app, SbaDatabase::class.java, "sba_database")
            .fallbackToDestructiveMigration().build()

    @Provides
    fun provideUserDetailsDao(db: SbaDatabase) = db.userDetailsDao()

    @Provides
    fun provideGarbageCollectionDao(db: SbaDatabase) = db.garbageCollectionDao()

    @Provides
    fun provideLocationDao(db: SbaDatabase) = db.locationDao()

    @Provides
    fun provideArchivedDao(db: SbaDatabase) = db.archivedDao()

    @Provides
    fun provideGisLocationDao(db: SbaDatabase) = db.gisLocationDao()

    @Provides
    fun provideTripDao(db: SbaDatabase) = db.tripDao()

    @Provides
    fun provideTripHouseDao(db: SbaDatabase) = db.tripHouseDao()

    @Provides
    fun provideEmpGcDao(db: SbaDatabase) = db.empGcDao()

    @Provides
    fun provideEmpHouseOnMapDao(db: SbaDatabase) = db.empHouseOnMapDao()

    @Provides
    fun providePropertyTypeDao(db: SbaDatabase) = db.propertyTypeDao()

    @Provides
    fun provideNearestLatLongDao(db: SbaDatabase) = db.nearestLatLngDao()

    @Provides
    fun provideUserTravelLocDao(db: SbaDatabase) = db.userTravelLocDao()

    /**
     * RETROFIT WITHOUT TOKEN
     */
    @Provides
    @Singleton
    @Named("Normal")
    fun provideRetrofit(
        @Named("Normal") client: OkHttpClient,
        converterFactory: Converter.Factory,
        scalarsConverterFactory: ScalarsConverterFactory
    ): Retrofit =
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(scalarsConverterFactory)
            .addConverterFactory(converterFactory)
            .client(client).build()

    @Provides
    @Singleton
    @Named("Normal")
    fun provideClient(@Named("Normal") interceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder().connectTimeout(3600L, TimeUnit.SECONDS)
            .writeTimeout(3600L, TimeUnit.SECONDS).readTimeout(3600L, TimeUnit.SECONDS)
            .addInterceptor(interceptor).build()

    @Provides
    @Singleton
    @Named("Normal")
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    fun provideConverterFactory(gson: Gson): Converter.Factory {

        return GsonConverterFactory.create(gson)
    }

    @Provides
    fun provideScalarsConverterFactory(): ScalarsConverterFactory {

        return ScalarsConverterFactory.create()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    fun provideTokenApi(@Named("Normal") retrofit: Retrofit): TokenApi {
        return retrofit.create(TokenApi::class.java)
    }

    /**
     *  RETROFIT WITH TOKEN
     */
    @Provides
    @Singleton
    @Named("Authorize")
    fun provideRetrofitAuthorize(
        @Named("Authorize") client: OkHttpClient, converterFactory: Converter.Factory,
        scalarsConverterFactory: ScalarsConverterFactory
    ): Retrofit =
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(scalarsConverterFactory)
            .addConverterFactory(converterFactory)
            .client(client).build()

    @Provides
    @Singleton
    @Named("Authorize")
    fun provideClientAuthorize(
        @Named("Normal") interceptor: HttpLoggingInterceptor,
        interceptorWithToken: Interceptor,
        authAuthenticator: AppAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder().connectTimeout(3600L, TimeUnit.SECONDS)
        .writeTimeout(3600L, TimeUnit.SECONDS).readTimeout(3600L, TimeUnit.SECONDS)
        .addInterceptor(interceptor)
        .addInterceptor(interceptorWithToken)
        .authenticator(authAuthenticator)
        .build()


    @Provides
    fun providesOkhttpInterceptor(sessionDataStore: SessionDataStore): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->

            val token = runBlocking(Dispatchers.Default) {
                sessionDataStore.getBearerToken.first()
            }
            val request =
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            chain.proceed(request)
        }
    }

    @Provides
    fun providesSessionDataStore(@ApplicationContext context: Context) = SessionDataStore(context)

    @Provides
    fun provideLoginApi(@Named("Authorize") retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    fun provideUpdateApi(@Named("Authorize") retrofit: Retrofit) : UpdateApi {
        return retrofit.create(UpdateApi::class.java)
    }

    @Provides
    fun provideGisApi(@Named("Authorize") retrofit: Retrofit): GisApi {
        return retrofit.create(GisApi::class.java)
    }

    @Provides
    fun provideNearestLatLngApi(@Named("Authorize") retrofit: Retrofit): NearestLatLngApi {
        return retrofit.create(NearestLatLngApi::class.java)
    }

    /** ghanta gadi apis */

    @Provides
    fun provideUserDetailsApi(@Named("Authorize") retrofit: Retrofit): UserDetailsApi {
        return retrofit.create(UserDetailsApi::class.java)
    }

    @Provides
    fun provideDutyApi(@Named("Authorize") retrofit: Retrofit): DutyApi {
        return retrofit.create(DutyApi::class.java)
    }

    @Provides
    fun provideGcApi(@Named("Authorize") retrofit: Retrofit): ScanQrApi {
        return retrofit.create(ScanQrApi::class.java)
    }

    @Provides
    fun provideWorkHistoryApi(@Named("Authorize") retrofit: Retrofit): WorkHistoryApi {
        return retrofit.create(WorkHistoryApi::class.java)
    }

    @Provides
    fun provideLocationApi(@Named("Authorize") retrofit: Retrofit): LocationApi {
        return retrofit.create(LocationApi::class.java)
    }

    @Provides
    fun provideDumpYardTripApi(@Named("Authorize") retrofit: Retrofit): DumpYardTripApi {
        return retrofit.create(DumpYardTripApi::class.java)
    }

    /** house scanify apis */

    @Provides
    fun provideEmpDutyApi(@Named("Authorize") retrofit: Retrofit): EmpDutyApi {
        return retrofit.create(EmpDutyApi::class.java)
    }

    @Provides
    fun provideEmpGcApi(@Named("Authorize") retrofit: Retrofit): EmpGcApi {
        return retrofit.create(EmpGcApi::class.java)
    }

    @Provides
    fun provideEmpWorkHistoryApi(@Named("Authorize") retrofit: Retrofit): EmpWorkHistoryApi {
        return retrofit.create(EmpWorkHistoryApi::class.java)
    }

    @Provides
    fun providePropertyTypeApi(@Named("Authorize") retrofit: Retrofit): PropertyTypeApi {
        return retrofit.create(PropertyTypeApi::class.java)
    }

    @Provides
    fun provideMasterPlateApi(@Named("Authorize") retrofit: Retrofit): MasterPlateApi {
        return retrofit.create(MasterPlateApi::class.java)
    }

    @Provides
    fun provideEmployeeApiService(@Named("Authorize") retrofit: Retrofit): EmployeeApiService {
        return retrofit.create(EmployeeApiService::class.java)
    }

    @Provides
    fun provideGetUlbDetailsApi(@Named("Authorize") retrofit: Retrofit): GetUlbDetails {
        return retrofit.create(GetUlbDetails::class.java)
    }

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
