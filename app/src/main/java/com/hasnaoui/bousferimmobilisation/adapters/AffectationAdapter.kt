package com.hasnaoui.bousferimmobilisation.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.hasnaoui.bousferimmobilisation.databinding.AffectedToRowBinding
import com.hasnaoui.bousferimmobilisation.models.AffectationModel
import com.hasnaoui.bousferimmobilisation.models.DetailModel


class AffectationAdapter (val items:MutableList<AffectationModel>,val etat:String):RecyclerView.Adapter<AffectationAdapter.ViewHolder> (){
    private lateinit var binding:AffectedToRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = AffectedToRowBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position ])

        val affectation = items[position]

    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: AffectedToRowBinding): RecyclerView.ViewHolder(itemView.root){
        fun bind(item: AffectationModel){
            binding.apply {
                tvTextInfo.text = item.name
                if(item.comment.toString() !="false"){

                   etComment.setText(item.comment.toString())
                }
                else{
                    etComment.setText("")
                }
                yesOrNo.isChecked = item.checked==true

                yesOrNo.setOnCheckedChangeListener{ _, b ->
                    item.checked = b
                }

                etComment.addTextChangedListener(object : TextWatcher {

                    override fun afterTextChanged(s: Editable) {}

                    override fun beforeTextChanged(s: CharSequence, start: Int,
                                                   count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int,
                                               before: Int, count: Int) {
                        item.comment = s
                    }
                })
                if(etat != "draft"){
                    yesOrNo.isClickable = false;
                    etComment.isFocusable = true;
                    etComment.isClickable = true;
                    etComment.isEnabled = true
                }
                tvItemId.text = item.id.toString()
            }
    }
}
}