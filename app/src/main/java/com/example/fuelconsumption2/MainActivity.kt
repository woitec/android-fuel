package com.example.fuelconsumption2

import android.os.Bundle
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

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter(tankingsSummaryViewModel.tankings)

//      val configuration: Configuration = configurationDao.getConfiguration()

        val tankingsView: RecyclerView = findViewById<RecyclerView?>(R.id.tankingsView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tankingsRecyclerAdapter
        }

        lifecycleScope.launch {
            tankingsSummaryViewModel.getAllTankings().collect { currentAllTankings ->
//                Log.i("TEST", currentAllTankings.joinToString(",\n"))
                tankingsSummaryViewModel.updateTankingsRecyclerView(currentAllTankings)
//                Log.i("TEST", tankingsSummaryViewModel.tankings.joinToString(",\n"))
                tankingsRecyclerAdapter.notifyDataSetChanged()
            }
        }
    }
}