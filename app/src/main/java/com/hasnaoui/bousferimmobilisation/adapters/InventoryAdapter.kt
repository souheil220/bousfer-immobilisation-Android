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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryAdapter.ViewHolder {
        return ViewHolder( ItemRowInventoryLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ProductDetails::class.java).apply {
                putExtra(Constants.FROM, "click")
                putExtra(Constants.EXIST, "true")
                putExtra(Constants.CODE, product.data.code)
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
                        .format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")).toString()
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

    inner class ViewHolder(val itemBinding: ItemRowInventoryLineBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: InventoryModel) {

            itemBinding.tvCategory.text = item.data.category_id[1].toString()
            itemBinding.tvTitle.text = item.asset_id[1].toString()
            itemBinding.tvLocation.text = item.data.location
//                tvCentreDeCout.text = item.data.centre_de_cout
            itemBinding.tvState.text = when (item.state) {
                    "draft" -> {
                        "Non inventori??"
                    }
                    "absent" -> {
                        "Absent"
                    }
                    "done" -> {
                        "Inventori??"
                    }
                    else -> {
                        ""
                    }
                }
            itemBinding.tvNumSerie.text = item.data.num_serie
            itemBinding.tvDateInventaire.text = item.date

                Picasso.get().load("${Constants.BASE_URL}/images/image_produit/${item.data.code.replace("/","")}/${item.data.id}image1.jpg").fit().centerCrop()
                    .into(itemBinding.productImage);
            }
        }

    }
