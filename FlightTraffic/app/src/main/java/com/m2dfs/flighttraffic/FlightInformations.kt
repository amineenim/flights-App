package com.m2dfs.flighttraffic

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class FlightInformations : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel : FlightsListViewModel
    private var mapView: MapView? = null
    private lateinit var flightStateModel: FightStateModelArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById<MapView>(R.id.mapViewMoreInformation)
        mapView!!.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(FlightsListViewModel::class.java)
        //Get position of the airsoft
        viewModel.getCurrentPostionOfClickedFlight()
        viewModel.getFlightStateListLiveData().observe(viewLifecycleOwner, Observer {
            flightStateModel = it
            if (flightStateModel.states != null) {
                val speed = view.findViewById<TextView>(R.id.vitesseLabel)
                speed.text = "Vitesse : " + flightStateModel.states!![0][9] + " m/s"

                System.out.println(speed)
                val altitude = view.findViewById<TextView>(R.id.altitudeLabel)
                altitude.text = "Altitude : " + flightStateModel.states!![0][7]
                System.out.println(altitude)

                val direction = view.findViewById<TextView>(R.id.verticalRate)
                if (flightStateModel.states!![0][11] != null && flightStateModel.states!![0][11]!!.toDouble() > 0) {
                    direction.text = "Direction : " + "montée"
                } else if (flightStateModel.states!![0][11] != null && flightStateModel.states!![0][11]!!.toDouble() < 0) {
                    direction.text = "Direction : " + "descente"
                } else {
                    direction.text = "Direction : " + "stable"
                }
            } else {
                Toast.makeText(context, "Aucune information disponible", Toast.LENGTH_LONG).show()
            }
            mapView!!.getMapAsync(this)
        })

        viewModel.findingDepartureAndArrivalFromCurrentTrackedAirport()
        viewModel.getFlightModelStateLiveData().observe(viewLifecycleOwner, Observer {
            view.findViewById<TextView>(R.id.callSignMoreInformation).text = "Fly number : " + it.callsign
            view.findViewById<TextView>(R.id.departLabelMoreInformation).text = it.estDepartureAirport
            view.findViewById<TextView>(R.id.arriverLabelMoreInformation).text = it.estArrivalAirport
            view.findViewById<TextView>(R.id.flyTimeMoreInformation).text = "%02d:%02d".format(Date(it.lastSeen * 1000 - it.firstSeen * 1000).hours, Date(it.lastSeen * 1000 - it.firstSeen * 1000).minutes)
            view.findViewById<TextView>(R.id.heureArriverLabelMoreInformation).text = "%02d:%02d".format(Date(it.lastSeen * 1000).hours, Date(it.lastSeen * 1000).minutes)
            view.findViewById<TextView>(R.id.heureDepartLabelMoreInformation).text = "%02d:%02d".format(Date(it.firstSeen * 1000).hours, Date(it.firstSeen * 1000).minutes)

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.flight_information_fragment, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FlightInformation.
         */

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FlightInformations().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (flightStateModel.states != null) {
            //Todo corrigé la position elle est pas normal.
            val position = LatLng(flightStateModel.states!![0][6]!!.toDouble(), flightStateModel.states!![0][5]!!.toDouble())
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(position).title("Airplane Position"))
        }

    }

    override fun onResume() {
        if (mapView != null) {
            mapView!!.onResume()
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (mapView != null) {
            mapView!!.onLowMemory()
        }
    }
}