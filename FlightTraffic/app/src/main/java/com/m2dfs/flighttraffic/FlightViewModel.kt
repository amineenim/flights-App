package com.m2dfs.flighttraffic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FlightViewModel : ViewModel() {

    private val fromCalendarLiveData = MutableLiveData(Calendar.getInstance())
    private val toCalendarLiveData = MutableLiveData(Calendar.getInstance())
    private val airportList = MutableLiveData(Utils.generateAirportList())

    fun getFromCalendarLiveData(): LiveData<Calendar> {
        return fromCalendarLiveData
    }

    fun getToCalendarLiveData(): LiveData<Calendar> {
        return toCalendarLiveData
    }

    fun getAirportLiveData(): LiveData<List<Airport>> {
        return airportList
    }

    enum class DateType {
        FROM, TO
    }

    fun updateCalendarLiveData(dateType: DateType, calendar: Calendar) {
        if(dateType == DateType.FROM) fromCalendarLiveData.value = calendar
        else toCalendarLiveData.value = calendar
    }

    fun doRequest(url: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended(url, HashMap())
            }
            if (result != null) {
                Log.d("doRequest", result)
            }
        }
    }

}