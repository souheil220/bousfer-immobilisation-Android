package com.hasnaoui.bousferimmobilisation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.models.DetailModel
import com.hasnaoui.bousferimmobilisation.databinding.ItemRowFragmentBinding

class DetailAdapter(val items:MutableList<DetailModel>):RecyclerView.Adapter<DetailAdapter.ViewHolder> (){
    private lateinit var binding:ItemRowFragmentBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowFragmentBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position ])
        val detail = items[position]
    }

    override fun getItemCount() = items.size

        inner class ViewHolder(itemView:ItemRowFragmentBinding):RecyclerView.ViewHolder(itemView.root){
        fun bind(item: DetailModel){
            binding.apply {
                tvName.text = item.title
                tvDetail.text = item.detail
                image.setImageResource(item.image)
            }
        }
    }
}