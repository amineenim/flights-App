package com.m2dfs.flighttraffic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class FlightsListViewModel : ViewModel() {
    private val _flightsLiveData = MutableLiveData<List<FlightModel>>()
    val flightsLiveData:LiveData<List<FlightModel>> = _flightsLiveData


    fun setFlightsData(response: String) {
        try {
            // Convert the JSON array to a list of FlightModel using Gson
            val flightsList = Gson().fromJson<List<FlightModel>>(
                response,
                object : TypeToken<List<FlightModel>>() {}.type
            )

            // Update the LiveData with the list of FlightModel
            _flightsLiveData.value = flightsList
        } catch (e: JsonSyntaxException) {
            // Handle parsing error
            e.printStackTrace()
        }
    }
}