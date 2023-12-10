package com.m2dfs.flighttraffic

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class FlightsListViewModel : ViewModel() {
    private val flightListLiveData = MutableLiveData<List<FlightModel>>(ArrayList())
    private val flightTrackListLiveData = MutableLiveData<FlightTrackModel>()
    private val flightStateListLiveData = MutableLiveData<FightStateModelArray>()
    private val flightModelStateLiveData = MutableLiveData<FlightModel>()
    private val clickedFlightLiveData = MutableLiveData<FlightModel>()

    fun getFlightModelStateLiveData(): LiveData<FlightModel> {
        return flightModelStateLiveData
    }

    fun getFlightTrackListLiveData(): LiveData<FlightTrackModel> {
        return flightTrackListLiveData
    }

    fun getFlightStateListLiveData(): LiveData<FightStateModelArray> {
        return flightStateListLiveData
    }

    fun getClickedFlightLiveData(): LiveData<FlightModel> {
        return clickedFlightLiveData
    }

    fun setClickedFlightLiveData(flight: FlightModel) {
        clickedFlightLiveData.value = flight
    }

    fun getFlightListLiveData(): LiveData<List<FlightModel>> {
        return flightListLiveData
    }

    private fun setFlightListLiveData(flights: List<FlightModel>) {
        flightListLiveData.value = flights
    }

    var begin: Long = 0
        set(value) {
            field = value
        }
    var end: Long = 0
        set(value) {
            field = value
        }
    var icao: String = ""
        set(value) {
            field = value
        }
    var isArrival: Boolean = true
        set(value) {
            field = value
        }


    fun doRequest(){
        viewModelScope.launch {
            var key = HashMap<String, String>()
            key["begin"] = begin.toString()
            key["end"] = end.toString()
            key["airport"] = icao

            Log.i("kan", "hnaa")
            val result = withContext(Dispatchers.IO) {
                if (isArrival){
                    RequestManager.getSuspended(RequestManager.FLIGHT_ARRIVAL_ENDPOINT, key)
                } else {
                    RequestManager.getSuspended(RequestManager.FLIGHT_DEPARTURE_ENDPOINT, key)
                }
            }

            if (result != null) {
                Log.i("Result", result)
                Log.i("daz", "hnaa")
                var flightList = ArrayList<FlightModel>()
                Log.i("hello", "kan hna")
                val jsonElement = JsonParser.parseString(result)
                if(jsonElement.isJsonArray){
                    val jsonArray = jsonElement.asJsonArray
                    for (flyobject in jsonArray){
                        flightList.add(Gson().fromJson(flyobject.asJsonObject, FlightModel::class.java))
                    }
                    flightListLiveData.value = flightList
                } else {
                    Log.i("jsonparse", "there is an issue with parsing the json into an array")
                }
            }else{
                Log.i("taktak", "result is null")
                // detruire cette activite et revenir sur la main avec un Toast
            }
        }
    }

    fun getPositionOfClickedFlight(){
        viewModelScope.launch {
            var key = HashMap<String, String>()
            key["time"] = clickedFlightLiveData.value!!.firstSeen.toString()
            key["icao24"] = clickedFlightLiveData.value!!.icao24

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended(RequestManager.TRACK_ENDPOINT, key)
            }

            if (result != null) {
                Log.i("Result", result)
                val gson = Gson()
                val jsonElement = gson.fromJson(result, JsonElement::class.java)
                flightTrackListLiveData.value = gson.fromJson(jsonElement, FlightTrackModel::class.java)

            }
        }
    }

    fun getCurrentPostionOfClickedFlight(){
        viewModelScope.launch {
            var key = HashMap<String, String>()
            //key.put("time", Date().time.toString())
            key["icao24"] = clickedFlightLiveData.value!!.icao24
            //key.put("icao24", "040141")

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended(RequestManager.STATE_ENDPOINT, key)
            }

            if (result != null) {
                Log.i("Result", result)

                val gson = Gson()
                val jsonElement = gson.fromJson(result, JsonElement::class.java)

                flightStateListLiveData.value = gson.fromJson(jsonElement, FightStateModelArray::class.java)
            } else {
                flightStateListLiveData.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun findingDepartureAndArrivalFromCurrentTrackedAirport(){
        viewModelScope.launch {
            var key = HashMap<String, String>()

            key["icao24"] = clickedFlightLiveData.value!!.icao24
            key["end"] = ((Date().time / 1000) + 86400 ).toString()
            key["begin"] = ((Date().time / 1000) - 86400).toString()
            //key.put("icao24", "040141")

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended(RequestManager.FLIGHT_AIRCRAFT_ENDPOINT, key)
            }

            if (result != null) {
                Log.i("Result", result)

                val jsonElement = JsonParser.parseString(result)
                var flightList = ArrayList<FlightModel>()
                if(jsonElement.isJsonArray){
                    val jsonArray = jsonElement.asJsonArray
                    for (flyobject in jsonArray){
                        flightList.add(Gson().fromJson(flyobject.asJsonObject, FlightModel::class.java))
                    }
                }else{
                    Log.d("jsonissue", "the result is not array of json")
                }

                System.out.println((Date().time / 1000))
                flightList.forEach() { flightModel ->
                    if (flightModel.firstSeen < (Date().time / 1000)
                        && flightModel.lastSeen + 60 > (Date().time / 1000)) {
                        println("here")
                        flightModelStateLiveData.value = flightModel
                    }
                }
            }
        }
    }
}







