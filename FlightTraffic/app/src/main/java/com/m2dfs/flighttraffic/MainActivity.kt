package com.m2dfs.flighttraffic
import MainViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import java.util.*


class MainActivity : AppCompatActivity(), DialogAirportChoice.DialogAirportChoiceListener {
    @RequiresApi(Build.VERSION_CODES.M)
    var isArrival: Boolean = false
    private lateinit var viewModel: MainViewModel
    private var chooseAirport: AirportData? = null

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_main)
        val switchSelection = this.findViewById<Switch>(R.id.switchSelection)
        this.viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val aeroportSelection = this.findViewById<ConstraintLayout>(R.id.aeroportSelectionLayout)

        switchSelection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchSelection.text = "Arriver"
                this.isArrival = true
            } else {
                switchSelection.text = "Départ"
                this.isArrival = false
            }
        }

        aeroportSelection.setOnClickListener { view: View ->
            openAirportChoiceDialog()
        }

        val beginDateLabel = findViewById<TextView>(R.id.departDateSelection)
        val endDateLabel = findViewById<TextView>(R.id.arriverDateSelection)
        beginDateLabel.setOnClickListener { showDatePickerDialog(MainViewModel.DateType.BEGIN) }
        endDateLabel.setOnClickListener { showDatePickerDialog(MainViewModel.DateType.END) }

        viewModel.getBeginDateLiveData().observe(this) {
            beginDateLabel.text = Utils.dateFormatterCustom(it.time)
        }

        viewModel.getEndDateLiveData().observe(this) {
            endDateLabel.text = Utils.dateFormatterCustom(it.time)
        }

        val searchButton = this.findViewById<ImageButton>(R.id.search)
        searchButton.setOnClickListener { view: View ->
            val begin = viewModel.getBeginDateLiveData().value!!.timeInMillis / 1000
            // Date de fin
            val end = viewModel.getEndDateLiveData().value!!.timeInMillis / 1000

            //Security
            if (this.chooseAirport == null){
                val toast = Toast.makeText(this, "Veuillez choisir un aeroport", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener
            }

            if (begin >= end){
                val toast = Toast.makeText(this, "Veuillez choisir des dates valides", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener
            }

            if ((end - begin) >= 604800 ){
                val toast = Toast.makeText(this, "7j d'écart maximum", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener
            }

            val intent = Intent(this, FlightListActivity::class.java)
            intent.putExtra("BEGIN", begin)
            intent.putExtra("END", end)
            intent.putExtra("IS_ARRIVAL", isArrival)
            intent.putExtra("ICAO", if(this.chooseAirport!!.icao != "") this.chooseAirport!!.icao else null)
            startActivity(intent)
        }


    }

    //https://gist.github.com/codinginflow/11e5acb69a91db8f2be0f8e495505d12
    fun openAirportChoiceDialog() {
        val airportChoice = DialogAirportChoice()
        airportChoice.show(supportFragmentManager, "example dialog")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun applyAirportTexts(airportData: AirportData) {
        this.chooseAirport = airportData
        val code = this.findViewById<TextView>(R.id.aeroportSelection)
        val localisation = this.findViewById<TextView>(R.id.emplacementSelection)
        code.text = airportData.code
        localisation.text = airportData.localisation
        Log.i("test Instance", this.isArrival.toString())
    }

    private fun showDatePickerDialog(dateType: MainViewModel.DateType) {
        // Date Select Listener.
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.updateCalendarLiveData(dateType, calendar)
            }

        val currentCalendar = if (dateType == MainViewModel.DateType.BEGIN) viewModel.getBeginDateLiveData().value else viewModel.getEndDateLiveData().value

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            currentCalendar!!.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}

