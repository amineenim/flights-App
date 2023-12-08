package com.m2dfs.flighttraffic

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
            key.put("begin", begin.toString())
            key.put("end", end.toString())
            key.put("airport", icao)

            val result = withContext(Dispatchers.IO) {
                if (isArrival){
                    RequestManager.getSuspended("https://opensky-network.org/api/flights/arrival", key)
                } else {
                    RequestManager.getSuspended("https://opensky-network.org/api/flights/departure", key)
                }
            }

            if (result != null) {
                Log.i("Result", result)
                val parser = JsonParser()
                val jsonElement = parser.parse(result)
                var flightList = ArrayList<FlightModel>()
                for (flyobject in jsonElement.asJsonArray){
                    flightList.add(Gson().fromJson(flyobject.asJsonObject, FlightModel::class.java))
                }

                flightListLiveData.value = flightList
            }
        }
    }

    fun getPositionOfClickedFlight(){
        viewModelScope.launch {
            var key = HashMap<String, String>()
            key.put("time", clickedFlightLiveData.value!!.firstSeen.toString())
            key.put("icao24", clickedFlightLiveData.value!!.icao24)

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended("https://opensky-network.org/api/tracks/all", key)
            }

            if (result != null) {
                Log.i("Result", result)
                val parser = JsonParser()
                val jsonElement = parser.parse(result)
                flightTrackListLiveData.value = Gson().fromJson(jsonElement, FlightTrackModel::class.java)
            }
        }
    }

    fun getCurrentPostionOfClickedFlight(){
        viewModelScope.launch {
            var key = HashMap<String, String>()
            //key.put("time", Date().time.toString())
            key.put("icao24", clickedFlightLiveData.value!!.icao24)
            //key.put("icao24", "040141")

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended("https://opensky-network.org/api/states/all", key)
            }

            if (result != null) {
                Log.i("Result", result)
                val parser = JsonParser()
                val jsonElement = parser.parse(result)
                flightStateListLiveData.value = Gson().fromJson(jsonElement, FightStateModelArray::class.java)
            } else {
                flightStateListLiveData.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun findingDepartureAndArrivalFromCurrentTrackedAirport(){
        viewModelScope.launch {
            var key = HashMap<String, String>()

            key.put("icao24", clickedFlightLiveData.value!!.icao24)
            key.put("end", ((Date().time / 1000) + 86400 ).toString())
            key.put("begin", ((Date().time / 1000) - 86400).toString())
            //key.put("icao24", "040141")

            val result = withContext(Dispatchers.IO) {
                RequestManager.getSuspended("https://opensky-network.org/api/flights/aircraft", key)
            }

            if (result != null) {
                Log.i("Result", result)
                val parser = JsonParser()
                val jsonElement = parser.parse(result)
                var flightList = ArrayList<FlightModel>()
                for (flyobject in jsonElement.asJsonArray){
                    flightList.add(Gson().fromJson(flyobject.asJsonObject, FlightModel::class.java))
                }

                System.out.println((Date().time / 1000))
                flightList.forEach() { flightModel ->
                    if (flightModel.firstSeen < (Date().time / 1000)
                        && flightModel.lastSeen + 60 > (Date().time / 1000)) {
                        System.out.println("here")
                        flightModelStateLiveData.value = flightModel
                    }
                }
            }
        }
    }
}







