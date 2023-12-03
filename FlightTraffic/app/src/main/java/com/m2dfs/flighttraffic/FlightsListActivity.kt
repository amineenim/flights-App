package com.m2dfs.flighttraffic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class FlightsListActivity : AppCompatActivity() {
    private lateinit var viewModel: FlightsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flights_list)

        val begin = intent.getLongExtra("FROM_TIMESTAMP", 0)
        val end = intent.getLongExtra("TO_TIMESTAMP", 0)
        val isArrival = intent.getBooleanExtra("IS_ARRIVAL", false)
        val icao = intent.getStringExtra("AIRPORT_ICAO")

        viewModel = ViewModelProvider(this).get(FlightsListViewModel::class.java)

        Log.i("MAIN ACTIVITY", "begin = $begin \n end = $end \n icao = $icao \n is arrival = $isArrival")
    }
}