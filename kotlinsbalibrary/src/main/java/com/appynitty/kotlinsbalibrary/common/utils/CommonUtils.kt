package com.appynitty.kotlinsbalibrary.common.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.provider.Settings
import android.util.Base64
import com.appynitty.kotlinsbalibrary.common.MyApplication
import java.nio.charset.StandardCharsets
import java.util.Calendar


class CommonUtils {

    companion object {

        val APP_ID = MyApplication.APP_ID

        val VERSION_CODE = MyApplication.VERSION_CODE
        val PACKAGE_NAME = MyApplication.PACKAGE_NAME



        //const val BASE_URL = "http://202.65.157.254:7570/"
        const val BASE_URL = "https://ictcoreapi.ictsbm.com/"
//        const val BASE_URL = "https://testapi.ictsbm.com"
        //  const val BASE_URL = "http://124.153.94.110:1010/"
        // const val BASE_URL = "http://202.65.157.254:7570/"

        //  const val BASE_URL = "http://103.241.147.9:1010"


        const val CONTENT_TYPE = "application/json"
        const val STATUS_SUCCESS = "success"
        const val GIS_STATUS_SUCCESS = "Success"
        const val STATUS_ERROR = "error"

        const val CONFIRM_LOGOUT_DIALOG = "confirmLogout"
        const val CONFIRM_OFF_DUTY_DIALOG = "confirmOffDuty"

        @SuppressLint("HardwareIds")
        fun getAndroidId(context: Context): String? {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }

        fun getBatteryStatus(application: Application): Int {
            val batteryManager =
                application.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }


        fun getYearList(): MutableList<String> {
            val spinnerList = ArrayList<String>()
            // spinnerList.add("Select Year")
            for (i in 0 downTo -5 + 1) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.YEAR, i)
                spinnerList.add(calendar[Calendar.YEAR].toString())
            }
            return spinnerList
        }

        fun getCurrentMonth(): Int {
            val currMonth = Calendar.getInstance()
            currMonth.add(Calendar.MONTH, 0)
            return currMonth[Calendar.MONTH]
        }

        fun getCurrentYear(): Int {

            val currYear = Calendar.getInstance()
            currYear.add(Calendar.YEAR, 0)
            return currYear[Calendar.YEAR]
        }

//        fun getEncodedAppId(): String {
//            val enAppId =
//                "ictsbm@" + APP_ID.substring(0, 2) + "@Shirdi." + APP_ID.substring(2, 4)
//            val data: ByteArray = enAppId.toByteArray(StandardCharsets.UTF_8)
//            return Base64.encodeToString(data, Base64.NO_WRAP)
//        }

        fun getEncodedAppId(): String {
            if (APP_ID.isNullOrEmpty() || APP_ID.length < 4) {
                // Prevent crash and return fallback
                return Base64.encodeToString("ictsbm@00@Shirdi.00".toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            }

            val enAppId =
                "ictsbm@" + APP_ID.substring(0, 2) + "@Shirdi." + APP_ID.substring(2, 4)
            val data: ByteArray = enAppId.toByteArray(StandardCharsets.UTF_8)
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }


        fun isAirplaneModeOn(context: Context): Boolean {
            return Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
        }
    }
}