package com.example.fuelconsumption2

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.repository.TankingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tankingsData = mutableListOf<Tanking>()
        val tankingsRecyclerViewAdapter = TankingsRecyclerAdapter(tankingsData)
        val tankingsView: RecyclerView = findViewById(R.id.tankingsView)
        tankingsView.layoutManager = LinearLayoutManager(this)
        tankingsView.adapter = tankingsRecyclerViewAdapter
        val db = TankingRepository(this)
        lifecycleScope.launch {
            tankingsData.plus(db.getAllTankings())
            tankingsRecyclerViewAdapter.notifyDataSetChanged()
        }
    }
}