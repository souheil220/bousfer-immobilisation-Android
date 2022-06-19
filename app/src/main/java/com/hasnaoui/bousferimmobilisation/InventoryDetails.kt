package com.hasnaoui.bousferimmobilisation


import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.CustomListItemAdapter
import com.hasnaoui.bousferimmobilisation.adapters.InventoryAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityInventoryDetailsBinding
import com.hasnaoui.bousferimmobilisation.databinding.DialogCustumListBinding
import com.hasnaoui.bousferimmobilisation.databinding.ToolbarBinding
import com.hasnaoui.bousferimmobilisation.models.AssetQRModel
import com.hasnaoui.bousferimmobilisation.models.InventoryModel
import com.hasnaoui.bousferimmobilisation.utils.Constants
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class InventoryDetails : AppCompatActivity() {
    private lateinit var binding: ActivityInventoryDetailsBinding
    private var dataList:ArrayList<InventoryModel> = ArrayList()
    private var  inv_id:Int = 0
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var toolBarBinding: ToolbarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInventoryDetailsBinding.inflate(layoutInflater)
        inv_id = intent.getIntExtra("inv_id",0)
        toolBarBinding = ToolbarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        loadInventoryLine(inv_id.toString(),"onCreate")


        binding.apply {
            createMyRecycle(dataList)

                val scanIcon: ImageView = findViewById(R.id.qr_scanner_o)

                scanIcon.setOnClickListener {
                    barcodeLauncher.launch(ScanOptions())
                }

            val filterIcon:ImageView = findViewById(R.id.filter_icon)

            filterIcon.setOnClickListener {
                filterInventoryDialog()
            }


            etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    search(s.toString())
                }


                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            swipeToRefresh()
        }



        }
    private fun swipeToRefresh(){
        binding.siwpeToRefreshFragment.setOnRefreshListener {
            dataList.clear()
            loadInventoryLine(inv_id.toString(),"onCreate swipe To refresh")
        }
    }

    private fun search(text:String){
        val filteredList = ArrayList<InventoryModel>()
        Log.e("Text",text)
        for (product in dataList) {
            if (product.data.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(product)
            }
        }

        createMyRecycle(filteredList)

    }

    private fun filterInventoryDialog(){
        val customDialog = Dialog(this@InventoryDetails)
        val binding:DialogCustumListBinding = DialogCustumListBinding.inflate(layoutInflater)
        customDialog.setContentView(binding.root)
        binding.tvTitle.text="Hello"
        val inventoryOption = Constants.inventoryOption()
        inventoryOption.add(0, Constants.ALL_OPTIONS)
        binding.rvList.layoutManager = LinearLayoutManager(this@InventoryDetails)
        val adapter = CustomListItemAdapter(this@InventoryDetails,inventoryOption,Constants.FILTER_SELECTION)
        binding.rvList.adapter = adapter
        customDialog.show()
    }

    private fun createMyRecycle(listOfProduct:ArrayList<InventoryModel>){
        binding.detailListNonInv.layoutManager =LinearLayoutManager(this@InventoryDetails)
        inventoryAdapter = InventoryAdapter(listOfProduct,inv_id)
        binding.detailListNonInv.adapter = inventoryAdapter
    }


    private val barcodeLauncher =   registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        try {

            if (result.contents == null) {
                Toast.makeText(this@InventoryDetails, "Cancelled", Toast.LENGTH_LONG).show()
                Log.e("Error", result.contents)
            } else {
                textMaker(result.contents.toString())

                Toast.makeText(this@InventoryDetails, "passsed ", Toast.LENGTH_SHORT).show()

            }

        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
        }
    }

    private fun textMaker(text:String){
        GlobalScope.launch {
            async { loadInventoryLineQRCode(text)}.await()
        }
    }

    private fun addIcon(photoDisplay: Int, photoNextTo: Int?, first: Boolean, idIcon: Int) {

        val mainLayout: RelativeLayout = findViewById(R.id.relativeLayout1)
        val image = ImageView(this@InventoryDetails)
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        if (first) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            image.id = idIcon
        } else {
            params.addRule(RelativeLayout.LEFT_OF, photoNextTo!!)
        }
        image.setImageResource(photoDisplay)
        mainLayout.addView(image, params);


    }

    private fun removeIcons(photo: Int) {
        val checkIcon: ImageView = findViewById(photo)
        (checkIcon.parent as ViewGroup).removeView(checkIcon)
    }

    private suspend fun loadInventoryLineQRCode(asset_name:String){
        val dataList:ArrayList<AssetQRModel> = ArrayList()
        Dexter.withContext(this@InventoryDetails).withPermission(Manifest.permission.INTERNET).
        withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                response?.let {
                    val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                    // launching a new coroutine
                    GlobalScope.launch (Dispatchers.Main){
                        val result = inventaireApi.getInventaireLineQRCode(asset_name)
                        if (result.body() != null)
                        // Checking the results

                            dataList.add(result.body()!![0])


                        val intent = Intent(this@InventoryDetails, ProductDetails::class.java).apply {
                            putExtra("from","QRCODE")
                            putExtra("etat","draft")
                            putExtra("category",dataList[0].category_id[1].toString())
                            putExtra("name", dataList[0].name)
                            putExtra("location", dataList[0].location)
                            putExtra("centre_de_cout", dataList[0].centre_de_cout)
                            putExtra("numSerie", dataList[0].num_serie)
                            putExtra("quantite", dataList[0].quantite.toInt())
                            putExtra("asset_id", dataList[0].id)
                            putExtra("inventory_id", asset_name)
                            if(dataList[0].employee_affected_id.isNotEmpty()){

                                putExtra("employee_affected_id", dataList[0].employee_affected_id[0].toString())
                                putExtra("employee_affected_id_name", dataList[0].employee_affected_id[1].toString())
                            }
//                putExtra("image", product.image)
                        }
                        startActivity(intent)





//                        inventoryAdapter.notifyDataSetChanged()
                    }


                }


//

            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response!!.isPermanentlyDenied) {
                    AlertDialog.Builder(this@InventoryDetails)
                        .setMessage(
                            "Vous avez refuser l'acces a la camera pour utuliser " +
                                    "cette fonctionalité veuillez " +
                                    "donner les persmission necessaire a cette aplication"
                        )
                        .setPositiveButton("Paramettre") { _, _ ->
                            run {
                                try {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri =
                                        Uri.fromParts("package",  this@InventoryDetails.packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        .setNegativeButton("Cancel"){dialog,_->
                            run {
                                dialog.dismiss()
                            }
                        }.show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest();
            }
        }).onSameThread().check()

    }

    private fun loadInventoryLine(inv_id:String,from:String) {
        Log.e("loadInventoryLine ","got to inventory loadInventoryLine from $from")
        val progressBar: ProgressBar = binding.progressBarFragment
        Dexter.withContext(this@InventoryDetails).withPermission(Manifest.permission.INTERNET).
        withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                response?.let {
                    val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                    // launching a new coroutine
                    GlobalScope.launch (Dispatchers.Main){
                        progressBar.visibility = View.VISIBLE
                        val result = inventaireApi.getInventaireLine(inv_id)

                        if (result.body() != null)
//                        Log.e("result ",inv_id)
                        // Checking the results
                            for (inv in 0 until  result.body()!!.size){

                                dataList.add(result.body()!![inv])

                            }
                        progressBar.visibility = View.INVISIBLE
                        inventoryAdapter.notifyDataSetChanged()
                    }

                    binding.siwpeToRefreshFragment.isRefreshing = false
                }


            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response!!.isPermanentlyDenied) {
                    AlertDialog.Builder(this@InventoryDetails)
                        .setMessage(
                            "Vous avez refuser l'acces a la camera pour utuliser " +
                                    "cette fonctionalité veuillez " +
                                    "donner les persmission necessaire a cette aplication"
                        )
                        .setPositiveButton("Paramettre") { _, _ ->
                            run {
                                try {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri =
                                        Uri.fromParts("package",  this@InventoryDetails.packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        .setNegativeButton("Cancel"){dialog,_->
                            run {
                                dialog.dismiss()
                            }
                        }.show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest();
            }
        }).onSameThread().check()


    }

}

