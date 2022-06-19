package com.hasnaoui.bousferimmobilisation.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.R
import com.hasnaoui.bousferimmobilisation.adapters.DetailAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityInventoryDetailsBinding
import com.hasnaoui.bousferimmobilisation.databinding.FragmentDetailsBinding
import com.hasnaoui.bousferimmobilisation.models.DetailModel

class DetailsFragment : Fragment() {


    private var binding:FragmentDetailsBinding?=null

    private var dataList:MutableList<DetailModel> = mutableListOf()
    private lateinit var detailAdapter: DetailAdapter

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater,container,false)

        // Inflate the layout for this fragment

        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.apply {
            loadData()
            detailAdapter = DetailAdapter(dataList)
            detailList.apply {
                layoutManager = LinearLayoutManager(view.context)
                adapter = detailAdapter
            }
        }
    }




    fun loadData(){
        dataList.add(DetailModel(R.drawable.ic_nav_user,"Name","INV GSH 2021"))
        dataList.add(DetailModel(R.drawable.ic_nav_sign_out,"Date De Debut D'inventaire","01/12/2021"))
        dataList.add(DetailModel(R.drawable.ic_user_place_holder,"SOCIETE","GROUPE DES SOCIETE HASNAOUI (GSH)"))

    }
}