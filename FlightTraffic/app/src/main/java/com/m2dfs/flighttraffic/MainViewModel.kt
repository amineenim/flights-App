package com.m2dfs.flighttraffic

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Calendar

class MainViewModel() : ViewModel() {

    private val fromCalendarLiveData = MutableLiveData(Calendar.getInstance())
    private val toCalendarLiveData = MutableLiveData(Calendar.getInstance())
    private val airportList = MutableLiveData(Utils.generateAirportList())
    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse
    val toastLiveData = MutableLiveData<String>()


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
        if(dateType == DateType.FROM) {
            fromCalendarLiveData.value = calendar
            /*Si date FROM > aujourd'hui :
                    date FROM = aujourd'hui
              Si date FROM > date TO :
                    date TO = date FROM
              Sinon si date FROM < date TO - 7j :
                    date TO = date FROM + 7j
            */
            if (fromCalendarLiveData.value!!.compareTo(Calendar.getInstance()) > 0) {
                fromCalendarLiveData.value = Calendar.getInstance()
                toastLiveData.value = "Les vols futurs ne sont pas pris en charge."
            }
            if (fromCalendarLiveData.value!! > toCalendarLiveData.value!!) {
                toCalendarLiveData.value = fromCalendarLiveData.value
                toastLiveData.value = "La première date doit précéder la seconde date."
            }
            else if (fromCalendarLiveData.value!!.timeInMillis < toCalendarLiveData.value!!.timeInMillis - 604800000L) {
                val newToDate = fromCalendarLiveData.value!!.clone() as Calendar
                newToDate.add(Calendar.DAY_OF_MONTH,7)
                toCalendarLiveData.value = newToDate
                toastLiveData.value = "L'intervalle de recherche ne peut pas dépasser une semaine."
            }
        }
        else {
            toCalendarLiveData.value = calendar
            /*Si date TO > aujourd'hui :
                    date TO = aujourd'hui
              Si date FROM > date TO :
                    date FROM = date TO
              Sinon si date TO < date FROM + 7j :
                    date FROM = date TO - 7j
            */
            if (toCalendarLiveData.value!!.compareTo(Calendar.getInstance()) > 0) {
                toCalendarLiveData.value = Calendar.getInstance()
                toastLiveData.value = "Les vols futurs ne sont pas pris en charge."
            }
            if (toCalendarLiveData.value!! < fromCalendarLiveData.value!!) {
                fromCalendarLiveData.value = toCalendarLiveData.value
                toastLiveData.value = "La première date doit précéder la seconde date."
            }
            else if (toCalendarLiveData.value!!.timeInMillis > fromCalendarLiveData.value!!.timeInMillis + 604800000L) {
                val newFromDate = toCalendarLiveData.value!!.clone() as Calendar
                newFromDate.add(Calendar.DAY_OF_MONTH,-7)
                fromCalendarLiveData.value = newFromDate
                toastLiveData.value = "L'intervalle de recherche ne peut pas dépasser une semaine."
            }
        }
    }

    fun doRequest(isArrival: Boolean, selectedAirportIndex: Int) {
        viewModelScope.launch {

            val fromCalendar = fromCalendarLiveData.value
            val toCalendar = toCalendarLiveData.value

            // Check if either of the calendars is null
            if (fromCalendar == null || toCalendar == null) {
                // Handle the error or display a message
                _apiResponse.postValue(ApiResponse.Error("Calendrier non defini"))
                return@launch

            }

            // Check if the arrival date is after the departure date
            if (toCalendar.before(fromCalendar)) {
                // Handle the error or display a message
                _apiResponse.postValue(ApiResponse.Error("date de depart doit etre avant celle d'arrivee"))
                return@launch
            }

            // Calculate the difference in milliseconds
            val differenceInMillis = toCalendar.timeInMillis - fromCalendar.timeInMillis
            val millisInWeek = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds

            // Check if the difference exceeds a week
            if (differenceInMillis > millisInWeek) {
                // Handle the error or display a message
                _apiResponse.postValue(ApiResponse.Error("vous etes limite a une semaine"))
                return@launch
            }



            try {
                val url = if (isArrival) RequestManager.FLIGHT_ARRIVAL_ENDPOINT else RequestManager.FLIGHT_DEPARTURE_ENDPOINT
                val params = HashMap<String, String>().apply {
                    put("airport", airportList.value!![selectedAirportIndex].icao)
                    put("begin", (fromCalendarLiveData.value!!.timeInMillis / 1000).toString())
                    put("end", (toCalendarLiveData.value!!.timeInMillis / 1000).toString())
                    Log.i("api", url)
                }
                val result = withContext(Dispatchers.IO){
                    RequestManager.getSuspended(url, params)
                }
                // if success, emettre l'etat de succes avec les donnees
                result?.let{
                    _apiResponse.postValue(ApiResponse.Success(it))
                    // set data in the flightsListViewModel
                    //flightsViewModel.set(it)
                }
            }catch(e : HttpException){
                if(e.code() == 404){
                    _apiResponse.postValue(ApiResponse.Error("ressource non trouvee"))
                }else if(e.code() == 500){
                    _apiResponse.postValue(ApiResponse.Error("serveur indisponible"))
                }
            }
            catch (e : Exception){
                // en cas d'erreur , emettre l'etat d'erreur avec le message d'erreur
                _apiResponse.postValue(ApiResponse.Error("une erreur s'est produite : ${e.message}"))
            }
        }
    }
}