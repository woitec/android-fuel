package com.example.fuelconsumption2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
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
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle
import com.example.fuelconsumption2.data.typeConverters.FuelTypeConverter
import com.example.fuelconsumption2.enums.FuelType
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "FuelConsumptionApp.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val tankingsSummaryViewModel by viewModels<TankingsSummaryViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TankingsSummaryViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return TankingsSummaryViewModel(db) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
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

        tankingsSummaryViewModel.initializeState()

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter()
        findViewById<RecyclerView?>(R.id.tankingsView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tankingsRecyclerAdapter
        }

        lifecycleScope.launch {
//            tankingsSummaryViewModel.state.collect { state ->
//                tankingsRecyclerAdapter.updateTankings(state.visibleTankings)
//            }
            tankingsSummaryViewModel.currentTankings.collect { currentTankings ->
                tankingsRecyclerAdapter.updateTankings(currentTankings)
            }
        }

        findViewById<Button>(R.id.buttonAddFuelConsumption).setOnClickListener {
            tankingsSummaryViewModel.onEvent(TankingEvent.ShowAddTankingDialog)
        }

        //TODO(">Handle all possible events below")
        lifecycleScope.launch {
            tankingsSummaryViewModel.events.collect { event ->
                when (event) {
                    is TankingEvent.ShowAddVehicleDialog -> TODO()
                    is TankingEvent.HideAddVehicleDialog -> TODO()

                    is TankingEvent.ShowAddTankingDialog -> {
                        val addTankingDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.add_tanking, null)
                        val addTankingDialog = AlertDialog.Builder(this@MainActivity)
                            .setView(addTankingDialogView)
                            .setTitle("Add tanking")
                            .create()

                        val fuelPick: Spinner = addTankingDialogView.findViewById(R.id.addTankingFuelType)
                        val fuelTypes = mutableListOf("No fuel selected").apply {
                            addAll(FuelType.entries.toTypedArray().map {
                                it.name
                            })
                        }
                        fuelPick.adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_item,
                            fuelTypes
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        fuelPick.setSelection(0) //Default to "No fuel selected"

                        val vehiclePick: Spinner = addTankingDialogView.findViewById(R.id.addTankingVehiclePick)

                        lifecycleScope.launch {
                            val recentVehicleId = tankingsSummaryViewModel.state.value.currentVehicle

                            val vehicles = tankingsSummaryViewModel.getAllVehiclesForAddingTanking()

                            val vehicleNames = mutableListOf("No vehicle selected").apply {
                                addAll(vehicles.map { it.Name ?: ( "Vehicle " + it.VehicleId + " with null name" ) })
                            }

                            vehiclePick.adapter = ArrayAdapter(
                                this@MainActivity,
                                android.R.layout.simple_spinner_item,
                                vehicleNames
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }

                            var selectedVehicle = Vehicle()
                            vehiclePick.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View, vehiclePosition: Int, id: Long) {
                                    if (vehicles.isNotEmpty()) selectedVehicle = vehicles[vehiclePosition]
                                    val fuelTypeName = selectedVehicle.DefaultFuelType?.name ?: "No fuel selected"
                                    fuelPick.setSelection(fuelTypes.indexOf(fuelTypeName).coerceAtLeast(0))
                                    //TODO("Unit test that shit. fuelTypes is passed hand-to-hand but the user may mess up the spinner somehow.")

                                    val kilometersBefore = selectedVehicle.Kilometers?.toString() ?: "No km stored"
                                    addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersBefore).setText(kilometersBefore)
                                }
                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }

                            /*
                                `recentVehicleId` is served by `ConfigurationRepository` and
                                `vehicles` are served by `VehicleRepository`
                                both using absolute indexes in `db`
                                but I'm adding a placeholder vehicle so they match 1:1+1
                                TODO("Unit test that shit.")
                            */
                            vehiclePick.setSelection((recentVehicleId?.plus(1)) ?: 0)

                            addTankingDialogView.findViewById<Button>(R.id.addTankingSubmit).setOnClickListener {
                                val fuelTypeText = fuelPick.selectedItem.toString()
                                val fuelType = if (fuelTypeText == "No fuel selected") {
                                    Toast.makeText(this@MainActivity, "Fuel type will be null.", Toast.LENGTH_SHORT).show()
                                    null
                                } else {
                                    FuelTypeConverter().toFuelType(fuelTypeText)
                                }
                                val kilometersBeforeText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersBefore).text.toString()
                                val kilometersAfterText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersAfter).text.toString()
                                val amountOfFuelText = addTankingDialogView.findViewById<EditText>(R.id.addTankingAmount).text.toString()
                                val priceText = addTankingDialogView.findViewById<EditText>(R.id.addTankingPrice).text.toString()

                                if (listOf(kilometersBeforeText, kilometersAfterText, amountOfFuelText, priceText, fuelTypeText).any { it.isBlank() }) {
                                    Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                    return@setOnClickListener
                                }

                                val newTanking = Tanking(
                                    TankingId = 0, // Room will auto-re-generate this ID
                                    VehicleId = selectedVehicle.VehicleId,
                                    KilometersBefore = kilometersBeforeText.toIntOrNull() ?: 0,
                                    KilometersAfter = kilometersAfterText.toIntOrNull() ?: 0,
                                    FuelAmount = amountOfFuelText.toFloatOrNull() ?: 0f,
                                    Timestamp = Instant.now().toEpochMilli(),
                                    Price = priceText.toFloatOrNull() ?: 0f,
                                    FuelType = fuelType,
                                    Cost = (priceText.toFloatOrNull() ?: 0f) * (amountOfFuelText.toFloatOrNull() ?: 0f)
                                )

                                lifecycleScope.launch {
                                    try {
                                        tankingsSummaryViewModel.insertTanking(newTanking)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Error adding Tanking: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        addTankingDialog.dismiss()
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Tanking added successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //tankingsSummaryViewModel.refreshVisibleTankings()
                                    }
                                }
                            }
                        }
                        addTankingDialog.show()
                    }

                    is TankingEvent.HideAddTankingDialog -> TODO()

                    is TankingEvent.ShowFilterDialog -> TODO()
                    is TankingEvent.HideFilterDialog -> TODO()
                    is TankingEvent.SetDefaultVehicle -> TODO()

                    is TankingEvent.SetCurrentVehicle -> TODO()

                    is TankingEvent.DeleteTanking -> TODO()
                    is TankingEvent.EditTanking -> TODO()
                }
            }
        }
    }
}