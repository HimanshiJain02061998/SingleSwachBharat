package com.appynitty.kotlinsbalibrary.common.model.response

//data class AttendanceResponse(
//    val status: String?,
//    val code: Int?,
//    val message: String?,
//    val messageMar: String?,
//)
data class AttendanceResponse(
    val code: Int,
    val status: String?,
    val message: String?,
    val gismessage: String?,
    val messageMar: String?,
    val giserrorMessages: String?,
    val isAttendenceOn: Boolean,
    val isAttendenceOff: Boolean,
    val emptype: String?,
    val applink: String?,
    val houseid: Int,
    val IsExixts: Boolean,
    val referenceID: String?,   // NULL → string nullable
    val dyId: Int,
    val timestamp: String?,     // NULL → string nullable
    val Id: Int,
    val daId: Int,
    val isForceUpdate: Boolean?, // NULL → Boolean?
    val isValidDate: Boolean,
    val version: String?,        // NULL → string nullable
    val dutyStatus: String?      // NULL → string nullable
)
