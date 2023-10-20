package com.m2dfs.flighttraffic

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var flightViewModel: FlightViewModel
    private lateinit var fromDateTextView: TextView
    private lateinit var toDateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flightViewModel = ViewModelProvider(this).get(FlightViewModel::class.java)

        fromDateTextView = findViewById<TextView>(R.id.TextViewDateFrom)
        toDateTextView = findViewById<TextView>(R.id.TextViewDateTo)
        fromDateTextView.setOnClickListener { showDatePickerDialog(FlightViewModel.DateType.FROM) }
        toDateTextView.setOnClickListener { showDatePickerDialog(FlightViewModel.DateType.TO) }

        flightViewModel.getFromCalendarLiveData().observe(this) {
            fromDateTextView.text = Utils.dateToString(it.time)
        }

        flightViewModel.getToCalendarLiveData().observe(this) {
            toDateTextView.text = Utils.dateToString(it.time)
        }

        //Liste aéroports
        val spinner = findViewById<Spinner>(R.id.airportSpinner)
        flightViewModel.getAirportLiveData().observe(this) {
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, it)
        }

        //Exemple requête
        flightViewModel.doRequest("https://opensky-network.org/api/flights/arrival?airport=LFPO&begin=1572172110&end=1572258510")
    }

    private fun showDatePickerDialog(dateType: FlightViewModel.DateType) {
        val dateSetListener = OnDateSetListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            flightViewModel.updateCalendarLiveData(dateType, calendar)
        }

        val currentCalendar = if (dateType == FlightViewModel.DateType.FROM) flightViewModel.getFromCalendarLiveData().value else flightViewModel.getToCalendarLiveData().value

        currentCalendar?.let { calendar ->
            val datePickerDialog = DatePickerDialog(
                    this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.show()
        }
    }
}