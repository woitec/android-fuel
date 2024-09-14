package com.example.fuelconsumption2

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking

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

//        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db-tankings-app").build()

//        val configurationDao = db.configurationDao()
//        val tankingDao = db.tankingDao()
//        val vehicleDao = db.vehicleDao()

//        val configuration: Configuration = configurationDao.getConfiguration()
//        val tankings: List<Tanking> = tankingDao.getAllTankings()
//        val tankingsRecyclerAdapter = TankingsRecyclerAdapter(tankings)

        val tankingsRecyclerAdapter = TankingsRecyclerAdapter(listOf(
            Tanking(0,0,0,10, 14.8f, "14.09.2024", "21:43", 2.49f, "LPG", 36.85f),
            Tanking(1,0,0,10, 14.8f, "14.09.2024", "21:43", 2.49f, "LPG", 36.85f),
            Tanking(2,0,0,10, 14.8f, "14.09.2024", "21:43", 2.49f, "LPG", 36.85f),
            Tanking(3,0,0,10, 14.8f, "14.09.2024", "21:43", 2.49f, "LPG", 36.85f)
        ))

        val tankingsView: RecyclerView = findViewById(R.id.tankingsView)
        tankingsView.layoutManager = LinearLayoutManager(this)
        tankingsView.adapter = tankingsRecyclerAdapter
    }
}