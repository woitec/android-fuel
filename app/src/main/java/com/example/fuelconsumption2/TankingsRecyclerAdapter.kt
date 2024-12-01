package com.example.fuelconsumption2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.typeConverters.FuelTypeConverter

class TankingsRecyclerAdapter(private var tankings: List<Tanking>): RecyclerView.Adapter<TankingsRecyclerAdapter.ViewHolder>() {
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
        holder.dateText.text = SteroidDate(tankings[position].Timestamp).displayDate()
        holder.timeText.text = SteroidDate(tankings[position].Timestamp).displayTime()
        holder.fuelTypeText.text = FuelTypeConverter().fromFuelType(tankings[position].FuelType)
        holder.fuelAmountText.text = tankings[position].FuelAmount.toString()
        holder.priceText.text = tankings[position].Price.toString()
        holder.costText.text = tankings[position].Cost.toString()
        holder.kilometersDifferenceText.text = ((tankings[position].KilometersAfter ?: 0) - (tankings[position].KilometersBefore ?: 0)).toString()
    }

    fun updateTankings(newTankings: List<Tanking>) {
        tankings = newTankings
        notifyDataSetChanged()
    }
}