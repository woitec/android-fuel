package com.example.fuelconsumption2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle
import com.example.fuelconsumption2.data.repository.ConfigurationRepository
import com.example.fuelconsumption2.data.repository.TankingRepository
import com.example.fuelconsumption2.data.repository.VehicleRepository
import com.example.fuelconsumption2.data.typeConverters.FuelTypeConverter
import com.example.fuelconsumption2.enums.FuelType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale
import kotlin.math.tan

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db.tankingDao())
    private val vehicleRepository = VehicleRepository(db.vehicleDao())
    private val configurationRepository = ConfigurationRepository(db.configurationDao())

    private val _state = MutableStateFlow(TankingsSummaryState())
    val state: StateFlow<TankingsSummaryState> = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TankingsSummaryState())

    private val _events = MutableSharedFlow<TankingEvent>()
    val events: SharedFlow<TankingEvent> = _events.asSharedFlow()

    fun initiateTankingsSummaryState() {
        val currentTimestamp = Instant.now()

        val historyStartFromStateOrInitial = if (_state.value.historyFilterEnd != null) {
            _state.value.historyFilterEnd
        } else {
            SteroidDate.oneYearBefore(currentTimestamp).getTimestamp()
        }

        val historyEndFromStateOrInitial = if (_state.value.historyFilterStart != null) {
            _state.value.historyFilterStart
        } else {
            SteroidDate(currentTimestamp.toEpochMilli()).getTimestamp()
        }

        viewModelScope.launch {
            if(configurationRepository.isConfigurationEmpty()) {
                val defaultConfiguration = Configuration(1, null, null)
                configurationRepository.insertConfiguration(defaultConfiguration)
            }

            val recentVehicleId: Int? = configurationRepository.getRecentVehicleId()

            val visibleTankingsFetched = tankingRepository.getAllTankingsInBetweenByVehicleId(
                recentVehicleId,
                historyStartFromStateOrInitial,
                historyEndFromStateOrInitial
            )

            val totalFuel = visibleTankingsFetched.fold(0f) { acc, tanking ->
                acc + (tanking.FuelAmount ?: 0f)
            }

            val kilometersBefore = visibleTankingsFetched.firstOrNull()?.KilometersBefore ?: 0
            val kilometersAfter = visibleTankingsFetched.lastOrNull()?.KilometersAfter ?: 0
            val totalKm = kilometersBefore - kilometersAfter

            val totalCost = visibleTankingsFetched.fold(0f) { acc, tanking ->
                acc + (tanking.Cost ?: 0f)
            }
            val averageCost = totalCost / totalKm * 100

            val averageConsumption = if (totalKm > 0) {
                (totalFuel / totalKm) * 100
            } else {
                0f
            }

            _state.update {
                it.copy(
                    visibleTankings = visibleTankingsFetched,
                    currentDate = SteroidDate(currentTimestamp.toEpochMilli()),
                    averageConsumption = averageConsumption,
                    averageCost = averageCost,
                    currentVehicle = recentVehicleId,
                    historyFilterStart = historyStartFromStateOrInitial,
                    historyFilterEnd = historyEndFromStateOrInitial
                )
            }
        }
    }

    fun onEvent(event: TankingEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    //EVENTS
    fun showAddTankingDialog(context: Context) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isAddingTanking = true)
            }
        }

        val addTankingDialogView = LayoutInflater.from(context).inflate(R.layout.add_tanking, null)
        val addTankingDialog = AlertDialog.Builder(context)
            .setView(addTankingDialogView)
            .setTitle("Input data")
            .create()

        val fuelPick: Spinner = addTankingDialogView.findViewById(R.id.addTankingFuelType)
        val fuelTypes = mutableListOf("No fuel selected").apply {
            addAll(FuelType.entries.toTypedArray().map {
                it.name
            })
        }
        val fuelPickAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, fuelTypes)
        fuelPickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fuelPick.adapter = fuelPickAdapter

        fuelPick.setSelection(0) //Default to "No fuel selected"

        val vehiclePick: Spinner = addTankingDialogView.findViewById(R.id.addTankingVehiclePick)

        viewModelScope.launch {
            val recentVehicleId = getRecentVehicleId()

            val vehicles = vehicleRepository.getAllVehiclesForAddingTanking()

            var selectedVehicle = Vehicle(VehicleId = -1, Name = "Vehicle -1 with null name", RegistryNumber = null, Kilometers = null, DefaultFuelType = null)

            val vehicleNames = mutableListOf("No vehicle selected")
            vehicleNames.addAll(vehicles.map { it.Name ?: ( "Vehicle " + it.VehicleId + " with null name" ) })

            val vehiclePickAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, vehicleNames)
            vehiclePickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            vehiclePick.adapter = vehiclePickAdapter

            vehiclePick.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (vehicles.isNotEmpty()) selectedVehicle = vehicles[position]

                    updateAddTankingPopupOnVehicleSelection(selectedVehicle, fuelTypes, addTankingDialogView)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // it will never happen because "No vehicle selected" is the hard-coded default selection
                }
            }

            if(recentVehicleId == null) {
                vehiclePick.setSelection(0)
            } else {
                /*
                    `recentVehicleId` is served by `ConfigurationRepository` and
                    `vehicles` are served by `VehicleRepository`
                    both using absolute indexes in `db`
                    but I'm adding a placeholder vehicle so they match 1:1+1
                    TODO("Unit test that shit.")
                */
                vehiclePick.setSelection(recentVehicleId+1)
            }

            addTankingDialogView.findViewById<Button>(R.id.addTankingSubmit).setOnClickListener {
                val fuelTypeText = fuelPick.selectedItem.toString()
                val fuelType: FuelType? = if (fuelTypeText == "No fuel selected") {
                    Toast.makeText(context, "Fuel type will be null.", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    FuelTypeConverter().toFuelType(fuelTypeText)
                }
                val kilometersBeforeText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersBefore).text.toString()
                val kilometersAfterText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersAfter).text.toString()
                val amountOfFuelText = addTankingDialogView.findViewById<EditText>(R.id.addTankingAmount).text.toString()
                val priceText = addTankingDialogView.findViewById<EditText>(R.id.addTankingPrice).text.toString()

                if (kilometersBeforeText.isBlank() || kilometersAfterText.isBlank() ||
                    amountOfFuelText.isBlank() || priceText.isBlank() || fuelTypeText.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val kilometersBefore = kilometersBeforeText.toIntOrNull() ?: 0
                val kilometersAfter = kilometersAfterText.toIntOrNull() ?: 0
                val amountOfFuel = amountOfFuelText.toFloatOrNull() ?: 0f
                val price = priceText.toFloatOrNull() ?: 0f
                val cost = String.format(Locale.US, "%.2f", (price * amountOfFuel)).toFloat()
                val currentTimestamp = Instant.now().toEpochMilli()

                val newTanking = Tanking(
                    TankingId = 0, // Room will auto-re-generate this ID
                    VehicleId = selectedVehicle.VehicleId,
                    KilometersBefore = kilometersBefore,
                    KilometersAfter = kilometersAfter,
                    FuelAmount = amountOfFuel,
                    Timestamp = currentTimestamp,
                    Price = price,
                    FuelType = fuelType,
                    Cost = cost
                )

                viewModelScope.launch {
                    try {
                        tankingRepository.insertTankings(newTanking)
                        Toast.makeText(context, "Tanking added successfully", Toast.LENGTH_SHORT).show()
                        addTankingDialog.dismiss()

                        val currentTimestampForState = Instant.now()
                        val currentSteroidDate = SteroidDate(currentTimestampForState.toEpochMilli())
                        val oneYearBeforeNowTimestamp = SteroidDate.oneYearBefore(currentTimestampForState).getTimestamp()

                        _state.update {
                            it.copy(
                                currentDate = currentSteroidDate,
                                isAddingTanking = false,
                                currentVehicle = selectedVehicle.VehicleId,
                                historyFilterStart = oneYearBeforeNowTimestamp,
                                historyFilterEnd = currentTimestampForState.toEpochMilli()
                            )
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error adding Tanking: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        val currentTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(_state.value.currentVehicle, _state.value.historyFilterStart, _state.value.historyFilterEnd)
                        _state.update {
                            it.copy(visibleTankings = currentTankings)
                        }
                    }
                }
            }
        }
        addTankingDialog.show()
    }

    private fun updateAddTankingPopupOnVehicleSelection(vehicle: Vehicle, fuelTypes: MutableList<String>, context: View) {
        val fuelTypeName = vehicle.DefaultFuelType?.name ?: "No fuel selected"
        val position = fuelTypes.indexOf(fuelTypeName)
        val fuelTypeSpinner = context.findViewById<Spinner>(R.id.addTankingFuelType)
        if (position >= 0) {
            fuelTypeSpinner.setSelection(position)
        } else {
            fuelTypeSpinner.setSelection(0)
        }
        //TODO("Unit test that shit. fuelTypes is passed hand-to-hand but the user may mess up the spinner somehow.")

        context.findViewById<EditText>(R.id.addTankingKilometersBefore).setText(vehicle.Kilometers?.toString() ?: "No km stored")
    }

    //TANKINGS
    suspend fun insertTankings(vararg newTanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTankings(*newTanking)
        }
    }

    suspend fun insertTanking(tanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTankings(tanking)

            val currentTimestampForState = Instant.now()
            val currentSteroidDate = SteroidDate(currentTimestampForState.toEpochMilli())
            val oneYearBeforeNowTimestamp = SteroidDate.oneYearBefore(currentTimestampForState).getTimestamp()

            _state.update {
                it.copy(
                    currentDate = currentSteroidDate,
                    isAddingTanking = false,
                    currentVehicle = tanking.VehicleId,
                    historyFilterStart = oneYearBeforeNowTimestamp,
                    historyFilterEnd = currentTimestampForState.toEpochMilli()
                )
            }
        }
    }

    suspend fun refreshVisibleTankings() {
        val currentTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(_state.value.currentVehicle, _state.value.historyFilterStart, _state.value.historyFilterEnd)
        _state.update {
            it.copy(visibleTankings = currentTankings)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }

    //VEHICLES
    fun getVehicleById(vehicleId: Int): Flow<Vehicle> {
        return vehicleRepository.getVehicleById(vehicleId)
    }

    suspend fun getAllVehiclesForAddingTanking(): List<Vehicle> {
        return vehicleRepository.getAllVehiclesForAddingTanking()
    }

    //CONFIGURATION
    suspend fun getRecentVehicleId(): Int? {
        return configurationRepository.getRecentVehicleId()
    }
}