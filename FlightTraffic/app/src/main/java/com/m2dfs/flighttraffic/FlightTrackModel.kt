package com.m2dfs.flighttraffic

data class FlightTrackModel (val icao24: String,
                             val startTime: Int,
                             val endTime: Int,
                             val calllsign: Int,
                             val path: Array<Array<String>>
)