package com.hasnaoui.bousferimmobilisation.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.InventoryDetails
import com.hasnaoui.bousferimmobilisation.models.SampleModel
import com.hasnaoui.bousferimmobilisation.databinding.ItemRowListInventaireBinding
import com.hasnaoui.bousferimmobilisation.utils.Constants

class SampleAdapter(val items:MutableList<SampleModel>):RecyclerView.Adapter<SampleAdapter.ViewHolder> (){
    private lateinit var binding: ItemRowListInventaireBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowListInventaireBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position ])
       val inventory = items[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, InventoryDetails::class.java).apply {
                putExtra(Constants.INVENTORY_ID, inventory.id)
                putExtra(Constants.INVENTORY_TITLE, inventory.name)
            }
               it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: ItemRowListInventaireBinding):RecyclerView.ViewHolder(itemView.root){
            fun bind(item: SampleModel){
                binding.apply {
                    inventoryId.text = item.name

                    confirmation.text = item.state
                }
            }
    }
}