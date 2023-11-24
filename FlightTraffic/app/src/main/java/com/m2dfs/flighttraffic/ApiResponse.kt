package com.m2dfs.flighttraffic

sealed class ApiResponse {
    data class Success(val data : String) : ApiResponse()
    data class Error(val errorMessage : String) : ApiResponse()
}
