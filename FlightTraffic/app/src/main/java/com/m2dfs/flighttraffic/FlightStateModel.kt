package com.m2dfs.flighttraffic

data class FlightStateModel (val icao24: String,
                             val calllsign: Int,
                             val origin_country: String,
                             val time_position: Int,
                             val last_contact: Int,
                             val longitude: Double,
                             val latitude: Double,
                             val baro_altitude: Double,
                             val on_ground: Boolean,
                             val velocity: Double,
                             val true_track: Double,
                             val vertical_rate: Double,
                             val sensors: List<String>,
                             val geo_altitude: Double,
                             val squawk: String,
                             val spi: Boolean,
                             val position_source: Int,
                             val category: Int)

data class FightStateModelArray (
    val time: String,
    val states: List<List<String?>>?)