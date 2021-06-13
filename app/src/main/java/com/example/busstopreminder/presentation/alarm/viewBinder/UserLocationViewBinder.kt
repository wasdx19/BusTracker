package com.example.busstopreminder.presentation.alarm.viewBinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.example.busstopreminder.R
import com.example.busstopreminder.data.userLocations.UserLocationItemDTO
import kotlinx.android.synthetic.main.item_alarm.view.*

class UserLocationViewBinder(private val onClick: ((UserLocationItemDTO) -> Unit)) :
    ItemViewBinder<UserLocationItemDTO, UserLocationViewBinder.ViewHolder>() {

    override fun onBindViewHolder(
        holder: UserLocationViewBinder.ViewHolder,
        item: UserLocationItemDTO
    ) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ViewHolder(inflater.inflate(R.layout.item_alarm, parent, false))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: UserLocationItemDTO) = with(itemView) {
            locationAText.text = item.startLocation.locationName
            locationBText.text = item.endLocation.locationName

            setAlarm.setOnClickListener {
                onClick.invoke(item)
            }
        }
    }
}