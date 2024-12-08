package com.example.fuelconsumption2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.typeConverters.FuelTypeConverter

class TankingsRecyclerAdapter: RecyclerView.Adapter<TankingsRecyclerAdapter.ViewHolder>() {
    private var tankings: List<Tanking> = emptyList()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val dateText: TextView
        val timeText: TextView
        val fuelTypeText: TextView
        val fuelAmountText: TextView
        val priceText: TextView
        val costText: TextView
        val kilometersDifferenceText: TextView

        init {
            dateText = view.findViewById(R.id.date)
            timeText = view.findViewById(R.id.time)
            fuelTypeText = view.findViewById(R.id.fuelType)
            fuelAmountText = view.findViewById(R.id.fuelAmount)
            priceText = view.findViewById(R.id.price)
            costText = view.findViewById(R.id.cost)
            kilometersDifferenceText = view.findViewById(R.id.kilometersDifference)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tanking_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = tankings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tanking = tankings[position]
        holder.dateText.text = SteroidDate(tanking.Timestamp).displayDate()
        holder.timeText.text = SteroidDate(tanking.Timestamp).displayTime()
        holder.fuelTypeText.text = FuelTypeConverter().fromFuelType(tanking.FuelType)
        holder.fuelAmountText.text = tanking.FuelAmount.toString()
        holder.priceText.text = tanking.Price.toString()
        holder.costText.text = tanking.Cost.toString()
        holder.kilometersDifferenceText.text = ((tanking.KilometersAfter ?: 0) - (tanking.KilometersBefore ?: 0)).toString()
    }

    fun updateTankings(newTankings: List<Tanking>) {
        val diffCallback = TankingDiffCallback(tankings, newTankings)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tankings = newTankings
        diffResult.dispatchUpdatesTo(this)
    }
}