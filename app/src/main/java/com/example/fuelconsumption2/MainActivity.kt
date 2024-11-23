package com.example.fuelconsumption2

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Vehicle
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "FuelConsumptionApp.db"
        ).build()
    }

    private val tankingsSummaryViewModel by viewModels<TankingsSummaryViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TankingsSummaryViewModel(db) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tankingsSummaryViewModel.populateDefaults()

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter(tankingsSummaryViewModel.tankings)
        val tankingsView: RecyclerView = findViewById<RecyclerView?>(R.id.tankingsView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tankingsRecyclerAdapter
        }

        lifecycleScope.launch {
            tankingsSummaryViewModel.state.collect { state ->
                state.visibleTankings?.let { flow ->
                    flow.collect { tankingsList ->
                        tankingsSummaryViewModel.updateTankingsRecyclerView(tankingsList)
                        tankingsRecyclerAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        lifecycleScope.launch {
            tankingsSummaryViewModel.events.collect { event ->
                when (event) {
                    is TankingEvent.showAddVehicleDialog -> TODO()
                    is TankingEvent.hideAddVehicleDialog -> TODO()

                    is TankingEvent.showAddTankingDialog -> TODO()
                    is TankingEvent.hideAddTankingDialog -> TODO()
                    is TankingEvent.SetAmount -> TODO()
                    is TankingEvent.SetVehicle -> TODO()
                    is TankingEvent.SetKilometersBefore -> TODO()
                    is TankingEvent.SetKilometersAfter -> TODO()
                    is TankingEvent.SetPrice -> TODO()
                    is TankingEvent.SaveTanking -> TODO()

                    is TankingEvent.showFilterDialog -> TODO()
                    is TankingEvent.hideFilterDialog -> TODO()
                    is TankingEvent.SetDefaultVehicle -> TODO()

                    is TankingEvent.SetCurrentVehicle -> TODO()

                    is TankingEvent.DeleteTanking -> TODO()
                    is TankingEvent.EditTanking -> TODO()
                }
            }
        }
    }

    private fun showAddTankingPopup() {
        val addTankingDialogView = layoutInflater.inflate(R.layout.add_tanking, null)
        val addTankingDialog = AlertDialog.Builder(this)
            .setView(addTankingDialogView)
            .setTitle("Input data")
            .create()


        val vehiclePick: Spinner = addTankingDialogView.findViewById(R.id.addTankingVehiclePick)
        lifecycleScope.launch {
            val recentVehicleId = tankingsSummaryViewModel.getRecentVehicleId()

            tankingsSummaryViewModel.getAllVehiclesForAddingTanking().collect { vehicles ->
                val vehicleNames = mutableListOf("No vehicle selected")
                vehicleNames.addAll(vehicles.map { it.Name ?: ( "Vehicle " + it.VehicleId + " with null name" ) })

                val vehiclePickAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, vehicleNames)
                vehiclePickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vehiclePick.adapter = vehiclePickAdapter

                vehiclePick.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val selectedVehicle = vehicles[position]
                        updateAddVehiclePopupOnVehicleSelection(selectedVehicle)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // it will never happen because "No vehicle selected" is a hard-coded default selection
                    }
                }
                if(recentVehicleId == null) {
                    vehiclePick.setSelection(0)
                } else {
                    /*
                        `recentVehicleId` is served by `ConfigurationRepository` and
                        `vehicles` are served by `VehicleRepository`
                        both using absolute indexes in `db` so they match 1:1
                        TODO("Unit test that shit.")
                    */
                    vehiclePick.setSelection(recentVehicleId+1)
                }
            }
        }

        addTankingDialogView.findViewById<Button>(R.id.addTankingSubmit).setOnClickListener {
            //val selectedVehicle = vehiclePick.selectedItem as Vehicle
            //val selectedVehicleId = selectedVehicle.VehicleId

            addTankingDialog.dismiss()
        }

        addTankingDialog.show()
    }

    private fun updateAddVehiclePopupOnVehicleSelection(vehicle: Vehicle) {
        //update fields in the popup: addTankingFuelType, addTankingKilometersBefore
    }
}