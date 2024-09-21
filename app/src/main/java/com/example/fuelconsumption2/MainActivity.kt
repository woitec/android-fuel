package com.example.fuelconsumption2

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var tankingsSummaryViewModel: TankingsSummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tankingsSummaryViewModel = ViewModelProvider(this)[TankingsSummaryViewModel::class.java]

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter(tankingsSummaryViewModel.tankings)

//      val configuration: Configuration = configurationDao.getConfiguration()

        val tankingsView: RecyclerView = findViewById<RecyclerView?>(R.id.tankingsView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tankingsRecyclerAdapter
        }

        lifecycleScope.launch {
//            tankingsSummaryViewModel.updateTankings(
//                Tanking(0,0,0,10, 14.8f, "14.09.2024", "21:43", 2.49f, "LPG", 36.85f),
//                Tanking(1,0,0,20, 14.1f, "15.09.2024", "21:34", 2.49f, "LPG", 35.11f),
//                Tanking(2,0,0,30, 14.2f, "16.09.2024", "21:49", 2.49f, "LPG", 35.36f),
//                Tanking(3,0,0,40, 14.9f, "17.09.2024", "21:40", 2.49f, "LPG", 37.10f)
//        )

            tankingsSummaryViewModel.getAllTankings().collect { currentTankings ->
//                Log.i("TEST", currentTankings.joinToString(",\n"))
                tankingsSummaryViewModel.updateTankingsListView(currentTankings)
//                Log.i("TEST", tankingsSummaryViewModel.tankings.joinToString(",\n"))
                tankingsRecyclerAdapter.notifyDataSetChanged()
            }
        }
    }
}