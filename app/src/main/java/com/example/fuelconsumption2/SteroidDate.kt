package com.example.fuelconsumption2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SteroidDate(private val timestamp: Long?) {
    fun displayDate(): String {
        if(timestamp == null) {
            return "null"
        } else {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(date)
        }
    }
    fun displayTime(): String {
        if(timestamp == null) {
            return "null"
        } else {
            val date = Date(timestamp)
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return timeFormat.format(date)
        }
    }
    fun getTimestamp(): Long? {
        return timestamp
    }
}