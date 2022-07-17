package com.hasnaoui.bousferimmobilisation.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.InventoryDetails
import com.hasnaoui.bousferimmobilisation.databinding.ItemCostumListFilterLayoutBinding

class CustomListItemAdapter(
    private val activity: Activity,
    private val listItems: List<String>,
    private val activityFrom: Activity?,
    private val selection: String
) :
    RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCostumListFilterLayoutBinding =
            ItemCostumListFilterLayoutBinding.inflate(LayoutInflater.from(activity), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItems[position]

        holder.tvText.text = item

        holder.itemView.setOnClickListener {
            if(activityFrom is InventoryDetails){

                activityFrom.filterSelection(item)
            }

        }
    }


    override fun getItemCount(): Int {
        return listItems.size
    }

    class ViewHolder(view: ItemCostumListFilterLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Holds the TextView that will add each item to
        val tvText = view.tvText
    }
}