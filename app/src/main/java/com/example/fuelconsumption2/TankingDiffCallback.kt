package com.example.fuelconsumption2

import androidx.recyclerview.widget.DiffUtil
import com.example.fuelconsumption2.data.entities.Tanking

class TankingDiffCallback(private val oldList: List<Tanking>, private val newList: List<Tanking>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].TankingId == newList[newItemPosition].TankingId
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}