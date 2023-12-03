package com.m2dfs.flighttraffic

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        mainViewModel.apiResponse.observe(this, Observer {
            response ->
            run {
                when (response) {
                    is ApiResponse.Success -> {
                        // ouvrir une nouvelle activite avec les donnees
                        progressBar.visibility = View.GONE
                        val intent = Intent(this, FlightsListActivity::class.java )
                        startActivity(intent)
                    }
                    is ApiResponse.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
            if (dateType == MainViewModel.DateType.FROM) {
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            }
            else {
                mainViewModel.getFromCalendarLiveData().observe(this) { calendar ->
                    val minDate = calendar.timeInMillis
                    datePickerDialog.datePicker.minDate = minDate
                    datePickerDialog.datePicker.maxDate = minDate + 604800000
                }
            }
            datePickerDialog.show()
        }
    }

    private fun submitForm() {
        val begin = mainViewModel.getFromCalendarLiveData().value!!.timeInMillis / 1000
        val end = mainViewModel.getToCalendarLiveData().value!!.timeInMillis / 1000
        val isArrival = findViewById<Switch>(R.id.switch1).isChecked
        val airportIndex = findViewById<Spinner>(R.id.airportSpinner).selectedItemPosition
        val airport = mainViewModel.getAirportLiveData().value!![airportIndex]
        val icao = airport.icao
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        //mainViewModel.doRequest(isArrival, airportIndex)
        val intent = Intent(this, FlightsListActivity::class.java)

        intent.putExtra("FROM_TIMESTAMP",begin)
        intent.putExtra("TO_TIMESTAMP",end)
        intent.putExtra("IS_ARRIVAL",isArrival)
        intent.putExtra("AIRPORT_ICAO",icao)

        startActivity(intent)
    }
}