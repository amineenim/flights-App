package com.m2dfs.flighttraffic

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fromDateTextView: TextView
    private lateinit var toDateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        fromDateTextView = findViewById<TextView>(R.id.TextViewDateFrom)
        toDateTextView = findViewById<TextView>(R.id.TextViewDateTo)
        var submitButton = findViewById<Button>(R.id.buttonSubmit)

        fromDateTextView.setOnClickListener { showDatePickerDialog(MainViewModel.DateType.FROM) }
        toDateTextView.setOnClickListener { showDatePickerDialog(MainViewModel.DateType.TO) }
        submitButton.setOnClickListener { submitForm() }


        mainViewModel.getFromCalendarLiveData().observe(this) {
            fromDateTextView.text = Utils.dateToString(it.time)
        }

        mainViewModel.getToCalendarLiveData().observe(this) {
            toDateTextView.text = Utils.dateToString(it.time)
        }

        //Liste aéroports
        val spinner = findViewById<Spinner>(R.id.airportSpinner)
        mainViewModel.getAirportLiveData().observe(this) {
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, it)
        }

        //Exemple requête
        //mainViewModel.doRequest(false, 0)
    }

    private fun showDatePickerDialog(dateType: MainViewModel.DateType) {
        val dateSetListener = OnDateSetListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            mainViewModel.updateCalendarLiveData(dateType, calendar)
        }

        val currentCalendar = if (dateType == MainViewModel.DateType.FROM) mainViewModel.getFromCalendarLiveData().value else mainViewModel.getToCalendarLiveData().value

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

    private fun submitForm() {
        val isArrival = findViewById<Switch>(R.id.switch1).isChecked
        val airportIndex = findViewById<Spinner>(R.id.airportSpinner).selectedItemPosition
        mainViewModel.doRequest(isArrival, airportIndex)
    }
}