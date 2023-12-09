package com.m2dfs.flighttraffic
import MainViewModel
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider


data class AirportData (
    val code: String,
    val localisation: String,
    val icao: String
) {
    @Override
    public override fun toString(): String {
        return "        " + this.code + " - " + this.localisation
    }
}

class DialogAirportChoice : AppCompatDialogFragment() {
    private var listener: DialogAirportChoiceListener? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val view: View = inflater.inflate(R.layout.dialog_airport_choice, null)

        val airportList = ArrayList<AirportData>()
        viewModel.getAirportListLiveData().observe(this) {
            for (airport in it) {
                airportList.add(
                    AirportData(
                        airport.code,
                        "${airport.city} - ${airport.country}",
                        airport.icao
                    )
                )
            }
        }

        val listview = view.findViewById<ListView>(R.id.listView)
        val adapter = activity?.let {
            ArrayAdapter<AirportData>(
                it,
                android.R.layout.simple_spinner_item,
                airportList
            )
        }
        listview.setAdapter(adapter)

        listview.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            Log.i("here", "Position=$position")
            listener!!.applyAirportTexts(airportList.get(position))
            this.dismiss()
        })
        builder.setView(view)
            .setTitle("Choisir un aeroport")
            .setNegativeButton("cancel",
                DialogInterface.OnClickListener { dialogInterface, i -> })

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as DialogAirportChoiceListener
    }

    interface DialogAirportChoiceListener {
        fun applyAirportTexts(airportdata: AirportData)
    }
}