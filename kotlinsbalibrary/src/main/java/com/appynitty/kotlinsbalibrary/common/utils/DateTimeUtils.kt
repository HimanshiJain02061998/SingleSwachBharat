package com.appynitty.kotlinsbalibrary.common.utils

import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtils {


    companion object {

        private const val SERVER_DATE_FORMAT = "MM-dd-yyyy"
        private const val SERVER_DATE_FORMAT_LOCAL = "yyyy-MM-dd"
        private const val EMP_SERVER_DATE_FORMAT = "dd-MM-yyyy"
        private const val TITLE_DATE_FORMAT = "dd MMM yyyy"
        const val SYNC_OFFLINE_DATE_FORMAT = "dd-MMM-yyyy"
        private const val SEMI_MONTH_FORMAT = "MMM"
        private const val DATE_VALUE_FORMAT = "dd"
        private const val SERVER_TIME_FORMAT = "hh:mm a"
        private const val SERVER_TIME_24HR_FORMAT = "HH:mm"
        private const val SERVER_DATE_TIME_FORMAT = "MM-dd-yyyy HH:mm:ss"
        const val SERVER_DATE_TIME_FORMAT_LOCAL = "yyyy-MM-dd HH:mm:ss.SSS"
        const val SIMPLE_DATE_FORMAT = "dd-MM-yyyy hh:mm aa"
        private const val IMAGE_FILENAME_FORMAT = "yyyy-MM-dd'T'HHmmssSSS"
        const val GIS_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"

        fun getGisServiceTimeStamp(): String {
            val format = SimpleDateFormat(
                GIS_DATE_TIME_FORMAT,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

        fun getServerTime(): String {
            val format = SimpleDateFormat(
                SERVER_TIME_FORMAT,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

        fun getYyyyMMddDate(): String {
            val format = SimpleDateFormat(
                SERVER_DATE_FORMAT_LOCAL,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

        fun getServerDate(): String {
            val format = SimpleDateFormat(
                SERVER_DATE_FORMAT,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

        fun getScanningServerDate(): String {
            val format = SimpleDateFormat(
                SERVER_DATE_TIME_FORMAT_LOCAL,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

        fun getSimpleDateTime(): String {
            return SimpleDateFormat(
                SIMPLE_DATE_FORMAT,
                Locale.US
            ).format(System.currentTimeMillis())
        }

        fun getFileNameTimeStamp(): String {
            val format = SimpleDateFormat(
                IMAGE_FILENAME_FORMAT,
                Locale.ENGLISH
            )
            return format.format(Calendar.getInstance().time)
        }

    }
}