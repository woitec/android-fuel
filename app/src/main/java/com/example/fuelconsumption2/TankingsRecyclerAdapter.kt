package com.example.fuelconsumption2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelconsumption2.data.entities.Tanking

class TankingsRecyclerAdapter(private val tankingsList: List<Tanking>): RecyclerView.Adapter<TankingsRecyclerAdapter.ViewHolder>() {
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

    override fun getItemCount(): Int = tankingsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dateText.text = tankingsList[position].Date
        holder.timeText.text = tankingsList[position].Time
        holder.fuelTypeText.text = tankingsList[position].FuelType
        holder.fuelAmountText.text = tankingsList[position].FuelAmount.toString()
        holder.priceText.text = tankingsList[position].Price.toString()
        holder.costText.text = tankingsList[position].Cost.toString()
        holder.kilometersDifferenceText.text = (tankingsList[position].KilometersAfter - tankingsList[position].KilometersBefore).toString()
    }

}