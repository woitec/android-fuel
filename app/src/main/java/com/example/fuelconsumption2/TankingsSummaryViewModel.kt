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
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle
import com.example.fuelconsumption2.data.repository.ConfigurationRepository
import com.example.fuelconsumption2.data.repository.TankingRepository
import com.example.fuelconsumption2.data.repository.VehicleRepository
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
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db.tankingDao())
    private val vehicleRepository = VehicleRepository(db.vehicleDao())
    private val configurationRepository = ConfigurationRepository(db.configurationDao())
    private val _state = MutableStateFlow(TankingsSummaryState())
    val state: StateFlow<TankingsSummaryState> = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TankingsSummaryState())

    private val _events = MutableSharedFlow<TankingEvent>()
    val events: SharedFlow<TankingEvent> = _events.asSharedFlow()

    val tankings = mutableListOf<Tanking>()

    fun populateTankingsForLastYear() {
        val currentTimestamp = Instant.now()
        val currentTimestampInMilli = currentTimestamp.toEpochMilli()
        val oneYearBeforeNowTimestamp = currentTimestamp
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .minus(1, ChronoUnit.YEARS)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            val recentVehicleId: Int? = configurationRepository.getRecentVehicleId()
            val visibleTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(
                recentVehicleId,
                oneYearBeforeNowTimestamp,
                currentTimestampInMilli
            )

            visibleTankings.collect { tankings ->
                val totalFuel = tankings.fold(0f) { acc, tanking ->
                    acc + (tanking.FuelAmount ?: 0f)
                }

                val kilometersBefore = tankings.firstOrNull()?.KilometersBefore ?: 0
                val kilometersAfter = tankings.lastOrNull()?.KilometersAfter ?: 0
                val totalKm = kilometersBefore - kilometersAfter

                val totalCost = tankings.fold(0f) { acc, tanking ->
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
                        visibleTankings = visibleTankings,
                        currentDate = SteroidDate(currentTimestampInMilli),
                        averageConsumption = averageConsumption,
                        averageCost = averageCost,
                        currentVehicle = recentVehicleId,
                        historyFilterStart = SteroidDate(oneYearBeforeNowTimestamp),
                        historyFilterEnd = SteroidDate(currentTimestampInMilli)
                    )
                }
            }
        }
    }

    fun onEvent(event: TankingEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

//    fun updateTankings(
//        visibleTankings: Flow<List<Tanking>>?,
//        currentDate: SteroidDate?,
//        averageConsumption: Float?,
//        averageCost: Float?,
//        isAddingVehicle: Boolean,
//        isFilteringHistory: Boolean,
//        isAddingTanking: Boolean,
//        currentVehicle: Int?,
//        historyFilterStart: SteroidDate?,
//        historyFilterEnd: SteroidDate?
//    ) {
//        _state.update {
//            it.copy(
//                visibleTankings = visibleTankings,
//                currentDate = currentDate,
//                averageConsumption = averageConsumption,
//                averageCost = averageCost,
//                isAddingVehicle = isAddingVehicle,
//                isFilteringHistory = isFilteringHistory,
//                isAddingTanking = isAddingTanking,
//                currentVehicle = currentVehicle,
//                historyFilterStart = historyFilterStart,
//                historyFilterEnd = historyFilterEnd
//            )
//        }
//    }

    //EVENTS
    fun showAddTankingDialog(context: Context) {
        val addTankingDialogView = LayoutInflater.from(context).inflate(R.layout.add_tanking, null)
        val addTankingDialog = AlertDialog.Builder(context)
            .setView(addTankingDialogView)
            .setTitle("Input data")
            .create()

        val vehiclePick: Spinner = addTankingDialogView.findViewById(R.id.addTankingVehiclePick)
        viewModelScope.launch {
            val recentVehicleId = getRecentVehicleId()

            getAllVehiclesForAddingTanking().collect { vehicles ->
                var selectedVehicle = Vehicle(VehicleId = -1, Name = "Vehicle -1 with null name", RegistryNumber = null, Kilometers = null, DefaultFuelType = null)
                val vehicleNames = mutableListOf("No vehicle selected")
                vehicleNames.addAll(vehicles.map { it.Name ?: ( "Vehicle " + it.VehicleId + " with null name" ) })

                val vehiclePickAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, vehicleNames)
                vehiclePickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vehiclePick.adapter = vehiclePickAdapter

                vehiclePick.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        if (vehicles.isNotEmpty()) selectedVehicle = vehicles[position]

                        updateAddVehiclePopupOnVehicleSelection(selectedVehicle, addTankingDialogView)
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
                    val fuelType = addTankingDialogView.findViewById<EditText>(R.id.addTankingFuelType).text.toString()
                    val kilometersBeforeText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersBefore).text.toString()
                    val kilometersAfterText = addTankingDialogView.findViewById<EditText>(R.id.addTankingKilometersAfter).text.toString()
                    val amountOfFuelText = addTankingDialogView.findViewById<EditText>(R.id.addTankingAmount).text.toString()
                    val priceText = addTankingDialogView.findViewById<EditText>(R.id.addTankingPrice).text.toString()

                    if (kilometersBeforeText.isBlank() || kilometersAfterText.isBlank() ||
                        amountOfFuelText.isBlank() || priceText.isBlank() || fuelType.isBlank()) {
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
                            tankingRepository.insertTanking(newTanking)
                            Toast.makeText(context, "Tanking added successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error adding Tanking: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    addTankingDialog.dismiss()
                    onEvent(TankingEvent.hideAddTankingDialog)
                }

            }
        }

        addTankingDialog.show()
    }

    private fun updateAddVehiclePopupOnVehicleSelection(vehicle: Vehicle, context: View) {
        val fuelTypeName = vehicle.DefaultFuelType?.name ?: "No fuel selected"
        context.findViewById<EditText>(R.id.addTankingFuelType).setText(fuelTypeName)

        context.findViewById<EditText>(R.id.addTankingKilometersBefore).setText(vehicle.Kilometers?.toString() ?: "No km stored")
    }

    fun updateTankingsRecyclerView(eventData: List<Tanking>) {
        tankings.clear()
        tankings.addAll(eventData)
    }

    //TANKINGS
    suspend fun insertTankings(vararg newTanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTanking(*newTanking)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }

    //VEHICLES
    fun getVehicleById(vehicleId: Int): Flow<Vehicle> {
        return vehicleRepository.getVehicleById(vehicleId)
    }

    private fun getAllVehiclesForAddingTanking(): Flow<List<Vehicle>> {
        return vehicleRepository.getAllVehiclesForAddingTanking()
    }

    //CONFIGURATION
    private suspend fun getRecentVehicleId(): Int? {
        return configurationRepository.getRecentVehicleId()
    }
}