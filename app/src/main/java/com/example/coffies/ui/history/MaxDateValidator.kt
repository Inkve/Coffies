package com.example.coffies.ui.history

import android.os.Parcel
import com.google.android.material.datepicker.CalendarConstraints.DateValidator

class MaxDateValidator(private val maxDate: Long) : DateValidator {
    override fun isValid(date: Long): Boolean = date <= maxDate
    override fun writeToParcel(dest: Parcel, flags: Int) {}
    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<MaxDateValidator> {
        override fun createFromParcel(parcel: android.os.Parcel): MaxDateValidator {
            val maxDate = parcel.readLong()
            return MaxDateValidator(maxDate)
        }

        override fun newArray(size: Int): Array<MaxDateValidator?> = arrayOfNulls(size)
    }
}