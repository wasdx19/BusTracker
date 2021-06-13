package com.example.busstopreminder.presentation.main.viewBinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.example.busstopreminder.R
import com.example.busstopreminder.presentation.model.UIBusLineData
import kotlinx.android.synthetic.main.item_bus_lines.view.*

class BusLineViewBinder(private val alarmClicked: ((UIBusLineData) -> Unit)) :
    ItemViewBinder<UIBusLineData, BusLineViewBinder.ViewHolder>() {

    override fun onBindViewHolder(holder: BusLineViewBinder.ViewHolder, item: UIBusLineData) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ViewHolder(inflater.inflate(R.layout.item_bus_lines, parent, false))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: UIBusLineData) = with(itemView) {
            busNumber.text = item.busLine
            alarmButton.setOnClickListener {
                alarmClicked.invoke(item)
            }
        }
    }
}