package com.example.fuelconsumption2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import kotlinx.coroutines.launch

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

        tankingsSummaryViewModel.initiateTankingsSummaryState()

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter()
        findViewById<RecyclerView?>(R.id.tankingsView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tankingsRecyclerAdapter
        }

        lifecycleScope.launch {
            tankingsSummaryViewModel.state.collect { state ->
                tankingsRecyclerAdapter.updateTankings(state.visibleTankings)
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
                        //tankingsSummaryViewModel.state.isAddingTanking = true via VM fun using _state
                        tankingsSummaryViewModel.showAddTankingDialog(this@MainActivity)
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