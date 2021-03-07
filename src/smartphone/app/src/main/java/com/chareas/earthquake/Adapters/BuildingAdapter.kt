package com.chareas.earthquake.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chareas.earthquake.Activities.ViewAutopsy
import com.chareas.earthquake.Models.Building
import com.chareas.earthquake.R
import java.text.SimpleDateFormat
import java.util.*


class BuildingAdapter(var buildings: MutableList<Building>) :
    RecyclerView.Adapter<BuildingAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val owner: TextView = view.findViewById(R.id.b_owner)
        val address: TextView = view.findViewById(R.id.b_address)
        val registered: TextView = view.findViewById(R.id.b_registered)

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.building_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val building = buildings[position]
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.owner.text = "${building.firstName} ${building.lastName}"
        viewHolder.address.text = "${building.location["city"]} ${building.location["postalCode"]}, ${building.location["country"]}"

        // Create a DateFormatter object for displaying date in specified format.

        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm")

        // Create a calendar object that will convert the date and time value in milliseconds to date.

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(building.registered)
        viewHolder.registered.text = formatter.format(calendar.time)


        viewHolder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ViewAutopsy::class.java)
            intent.putExtra("building", building)
            view.context.startActivity(intent)
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = buildings.size

}
