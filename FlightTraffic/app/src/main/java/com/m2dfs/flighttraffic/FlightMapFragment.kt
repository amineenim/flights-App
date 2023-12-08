package com.m2dfs.flighttraffic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Date


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FlightMapFragment : Fragment(), OnMapReadyCallback {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel : FlightsListViewModel
    private var mapView: MapView? = null
    private lateinit var flightModel: FlightModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moreInformation = view.findViewById<Button>(R.id.plusInformation)
        moreInformation.setOnClickListener {
            //change fragment
            val isPhone = view.findViewById<FragmentContainerView>(R.id.fragment_map_container) == null

            val transaction = parentFragmentManager.beginTransaction()
            if (isPhone){
                //Si c'est un téléphone le fragment map container n'existe pas, la vue actuelle
                //est le fragment_List_Container
                transaction.replace(R.id.fragment_list_container, FlightInformations.newInstance("", ""))
            } else {
                transaction.replace(R.id.fragment_map_container, FlightInformations.newInstance("", ""))
            }
            transaction.addToBackStack(null)
            transaction.commit()
        }
        view.visibility = View.INVISIBLE
        view.isEnabled = false
        mapView = view.findViewById<MapView>(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(FlightsListViewModel::class.java)
        viewModel.getClickedFlightLiveData().observe(viewLifecycleOwner, Observer {
            view.visibility = View.VISIBLE
            view.isEnabled = true
            flightModel = it
            view.findViewById<TextView>(R.id.callSignInformation).text = "Fly number : " + it.callsign
            view.findViewById<TextView>(R.id.departLabelInformation).text = it.estDepartureAirport
            view.findViewById<TextView>(R.id.arriverLabelInformation).text = it.estArrivalAirport
            view.findViewById<TextView>(R.id.flyTimeInformation).text = "%02d:%02d".format(Date(it.lastSeen * 1000 - it.firstSeen * 1000).hours, Date(it.lastSeen * 1000 - it.firstSeen * 1000).minutes)
            view.findViewById<TextView>(R.id.heureArriverLabelInformation).text = "%02d:%02d".format(Date(it.lastSeen * 1000).hours, Date(it.lastSeen * 1000).minutes)
            mapView!!.getMapAsync(this)
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        viewModel.getPositionOfClickedFlight()

        viewModel.getFlightTrackListLiveData().observe(viewLifecycleOwner) {
            val polylineOptions = PolylineOptions()
            var firstItem = true
            var latitude: Double? = null
            var lontitude: Double? = null
            googleMap.clear()
            it.path.forEach {
                latitude = it[1].toDouble()
                lontitude = it[2].toDouble()
                polylineOptions.add(LatLng(latitude!!, lontitude!!))
                if (firstItem) {
                    firstItem = false
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(latitude!!, lontitude!!))
                            .title("Départ")
                    )
                }
            }
            if (latitude != null && lontitude != null) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude!!, lontitude!!))
                        .title("Arrivé")
                )
            }
            googleMap.addPolyline(polylineOptions)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.flight_map_fragment, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FlightMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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