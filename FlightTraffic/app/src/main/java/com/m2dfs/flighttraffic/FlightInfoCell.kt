package com.m2dfs.flighttraffic

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView

class FlightInfoCell : LinearLayout {

    lateinit var depDateTextView: TextView
    lateinit var depAirportTextView: TextView
    lateinit var depHourTextView: TextView
    lateinit var arrDateTextView: TextView
    lateinit var arrAirportTextView: TextView
    lateinit var arrHourTextView: TextView
    lateinit var flightDurationTextView: TextView
    lateinit var flightNameTextView: TextView


    constructor(context: Context?) : super(context) {
        initLayout()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initLayout()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout()
    }

    private fun bindViews() {
        // make the find view by ids for your view
        depDateTextView = findViewById(R.id.departureTime)
        depAirportTextView= findViewById(R.id.departureAireport)
        depHourTextView= findViewById(R.id.departureHour)
        arrDateTextView= findViewById(R.id.arrivalTime)
        arrAirportTextView= findViewById(R.id.arrivalAireport)
        arrHourTextView= findViewById(R.id.arrivalHour)
        flightDurationTextView= findViewById(R.id.flightDuration)
        flightNameTextView= findViewById(R.id.flightId)
    }

    fun bindData(flight: FlightModel) {
        Log.d("TAG", "message")
        //fill your views
        depDateTextView.text = flight.firstSeen.toString()
        depAirportTextView.text = flight.estDepartureAirport
        //depHourTextView.text =
        flightNameTextView.text = flight.callsign
        flightDurationTextView.text = (flight.lastSeen - flight.firstSeen).toString()
        arrDateTextView.text = flight.lastSeen.toString()
        arrAirportTextView.text = flight.estArrivalAirport
        //depHourTextView.text =
    }

    private fun initLayout() {
        LayoutInflater.from(context).inflate(R.layout.flight_list_row, this, true)
        bindViews()
    }

}