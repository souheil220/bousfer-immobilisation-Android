package com.hasnaoui.bousferimmobilisation.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.ProductDetails
import com.hasnaoui.bousferimmobilisation.R
import com.hasnaoui.bousferimmobilisation.databinding.ItemRowInventoryFragmentBinding
import com.hasnaoui.bousferimmobilisation.models.InventoryModel
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class InventoryAdapter(private val items: MutableList<InventoryModel>, private val inv_id: Int) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {
    private lateinit var binding: ItemRowInventoryFragmentBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowInventoryFragmentBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
        val product = items[position]
        holder.itemView.setOnClickListener {

            val intent = Intent(it.context, ProductDetails::class.java).apply {
                putExtra("exist", "true")
                putExtra("category", product.data.category_id[1].toString())
                putExtra("id", product.id)
                putExtra("name", product.asset_id[1].toString())
                putExtra("asset_id", product.asset_id[0].toString())
                putExtra("inv_id", inv_id)
                putExtra("location", product.data.location)
                putExtra("centre_de_cout", product.data.centre_de_cout)
                putExtra("etat", product.state)
                putExtra("numSerie", product.data.num_serie)
                putExtra("quality", product.quality)
                putExtra(
                    "dateInventory",
                    LocalDateTime.now(ZoneId.of("GMT+1"))
                        .format(DateTimeFormatter.ofPattern("y-MM-d H:mm:ss")).toString()
                )
                putExtra("comment", product.comment)
                putExtra("quantite", product.data.quantite.toInt())
                if (product.data.employee_affected_id.isNotEmpty()) {

                    putExtra(
                        "employee_affected_id",
                        product.data.employee_affected_id[0].toString()
                    )
                    putExtra(
                        "employee_affected_id_name",
                        product.data.employee_affected_id[1].toString()
                    )
                }
//                putExtra("image", product.image)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: ItemRowInventoryFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryModel) {
            binding.apply {
                tvCategory.text = item.data.category_id[1].toString()
                tvTitle.text = item.asset_id[1].toString()
                tvLocation.text = item.data.location
                tvCentreDeCout.text = item.data.centre_de_cout
                tvState.text = when (item.state) {
                    "draft" -> {
                        "Non inventorié"
                    }
                    "absent" -> {
                        "Absent"
                    }
                    "done" -> {
                        "Inventorié"
                    }
                    else -> {
                        ""
                    }
                }
                tvNumSerie.text = item.data.num_serie
                tvDateInventaire.text = item.date
//                if(item.image != null){
//
//                     decodeImage(item.image.toString())
//                }
//                else if(item.image2 != "" && item.image2!=null){
//                    Log.e("image2 ",item.image2)
//                    decodeImage(item.image2)
//                }
//                else if(item.image3 != "" && item.image3!=null){
//                    Log.e("image3 ",item.image3)
//                    decodeImage(item.image3)
//                }
//                else{
//                   val  image = ""
//                }
//                item.image?.let { productImage.setImageResource(it)
//                }
                Picasso.get().load("http://10.0.2.2:5000/images/${item.data.num_serie}.jpg").fit().centerCrop()
                    .into(productImage);
            }
        }

    }
    private fun decodeImage(imageEncoded:String){
        Log.e("image ","esssst")
        val imageBytes = Base64.decode(imageEncoded, Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.productImage.setImageBitmap(decodedImage)
    }
}