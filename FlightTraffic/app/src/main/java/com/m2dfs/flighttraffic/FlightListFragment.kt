package com.m2dfs.flighttraffic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class FlightListFragment : Fragment(), FlightsAdapter.OnCellClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel : FlightsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[FlightsListViewModel::class.java]

        viewModel.getFlightListLiveData().observe(viewLifecycleOwner, Observer {
            //findViewById<TextView>(R.id.textView).text = it.toString()
            Log.d("FlightListFragment", "Flight list size: ${it.size}")
            //Récupérer le recyclerView
            val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
            // Attacher un Adapter
            recyclerView.adapter = FlightsAdapter(it, this)
            // Attacher un LayoutManager
            recyclerView.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.flight_list_fragment, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FlightListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCellClicked(flightModel: FlightModel) {
        viewModel.setClickedFlightLiveData(flightModel)
        // Masquer le fragment actuel
        view?.visibility = View.GONE
        view?.isEnabled = false

        val isTablet = view?.findViewById<FragmentContainerView>(R.id.fragment_map_container) != null

        // Si c'est un téléphone, remplacer le FlightListFragment par le FlightMapFragment
        if (!isTablet) {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_list_container, FlightMapFragment.newInstance("", ""))
            transaction.addToBackStack(null)
            transaction.commit()
        } else {
            // Si c'est une tablette, afficher le FlightMapFragment
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_map_container, FlightMapFragment.newInstance("", ""))
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

}