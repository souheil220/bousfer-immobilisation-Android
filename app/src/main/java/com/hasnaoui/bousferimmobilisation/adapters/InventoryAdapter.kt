package com.hasnaoui.bousferimmobilisation.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.ProductDetails
import com.hasnaoui.bousferimmobilisation.databinding.ItemRowInventoryLineBinding
import com.hasnaoui.bousferimmobilisation.models.InventoryModel
import com.hasnaoui.bousferimmobilisation.utils.Constants
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class InventoryAdapter(private val items: MutableList<InventoryModel>, private val inv_id: Int,private val inv_title:String) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {
    private lateinit var binding: ItemRowInventoryLineBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowInventoryLineBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
        val product = items[position]
        holder.itemView.setOnClickListener {

            val intent = Intent(it.context, ProductDetails::class.java).apply {
                putExtra(Constants.FROM, "click")
                putExtra(Constants.EXIST, "true")
                putExtra(Constants.INVENTORY_TITLE, inv_title)
                putExtra(Constants.CATEGORY, product.data.category_id[1].toString())
                putExtra(Constants.INVENTORY_LINE_ID, product.id)
                putExtra(Constants.NAME, product.asset_id[1].toString())
                putExtra(Constants.ASSET_ID, product.asset_id[0].toString())
                putExtra(Constants.INVENTORY_ID, inv_id)
                putExtra(Constants.LOCATION, product.data.location)
                putExtra(Constants.CENTRE_DE_COUT, product.data.centre_de_cout)
                putExtra(Constants.ETAT, product.state)
                putExtra(Constants.NUMERO_DE_SERIE, product.data.num_serie)
                putExtra(Constants.QUALITY, product.quality)
                putExtra(
                    Constants.DATE_INVENTORY,
                    LocalDateTime.now(ZoneId.of("GMT+1"))
                        .format(DateTimeFormatter.ofPattern("y-MM-d H:mm:ss")).toString()
                )
                putExtra(Constants.COMMENT, product.comment)
                putExtra(Constants.QUANTITY, product.data.quantite.toInt())
                if (product.data.employee_affected_id.isNotEmpty()) {

                    putExtra(
                        Constants.EMPLOYEE_AFFECTED_TO_ID,
                        product.data.employee_affected_id[0].toString()
                    )
                    putExtra(
                        Constants.EMPLOYEE_AFFECTED_TO_NAME,
                        product.data.employee_affected_id[1].toString()
                    )
                }
//                putExtra("image", product.image)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: ItemRowInventoryLineBinding) :
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
//                Picasso.get().load("http://10.0.2.2:5000/images/${item.data.num_serie}.jpg").fit().centerCrop()
                Picasso.get().load("${Constants.BASE_URL}/images/ZDH6NFPT.jpg").fit().centerCrop()
                    .into(productImage);
            }
        }

    }
}