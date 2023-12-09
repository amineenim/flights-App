package com.m2dfs.flighttraffic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider


class FlightsListActivity : AppCompatActivity() {
    private lateinit var viewModel: FlightsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flights_list)

        this.viewModel = ViewModelProvider(this)[FlightsListViewModel::class.java]
        Log.i("debug", intent.getLongExtra("BEGIN", 0).toString())
        Log.i("debug", intent.getLongExtra("END", 0).toString())
        this.viewModel.begin = intent.getLongExtra("BEGIN", 0)
        this.viewModel.end = intent.getLongExtra("END", 0)
        this.viewModel.isArrival = intent.getBooleanExtra("IS_ARRIVAL", false)
        this.viewModel.icao = intent.getStringExtra("ICAO").toString()

        this.viewModel.doRequest()
        Log.i("heere", "passed here !")

        val isTablet = findViewById<FragmentContainerView>(R.id.fragment_map_container) != null
        viewModel.getClickedFlightLiveData().observe(this, Observer {
            // Afficher le bon vol
            Log.i("haha", "passed here ")
            //Si c'est le telephone alors on remplace le fragment de list par la map
            //Sinon il y a deux containers
            if (!isTablet) {
                //remplacer le fragment
                Log.i("heeere","test for mobile")
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_list_container, FlightMapFragment.newInstance("", ""))
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })
    }
}