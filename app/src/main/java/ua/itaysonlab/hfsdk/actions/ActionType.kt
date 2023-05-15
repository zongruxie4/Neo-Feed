package ua.itaysonlab.hfsdk.actions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ActionType : Parcelable {
    // Launch specific activity with meta bundle
    START_ACTIVITY,

    // Launch default SETTINGS activity with bundle
    START_PREF_ACTIVITY,

    // Send specific data back to AIDL [processFeedAction]
    AIDL_DATA,

    // Do nothing (seriously?)
    NOTHING
}