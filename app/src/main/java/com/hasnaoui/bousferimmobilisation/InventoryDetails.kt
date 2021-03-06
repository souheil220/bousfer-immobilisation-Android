package com.hasnaoui.bousferimmobilisation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.CustomListItemAdapter
import com.hasnaoui.bousferimmobilisation.adapters.InventoryAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityInventoryDetailsBinding
import com.hasnaoui.bousferimmobilisation.databinding.DialogCustumListBinding
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import android.view.Gravity





class InventoryDetails : AppCompatActivity() {
    private lateinit var binding: ActivityInventoryDetailsBinding
    private var dataList: ArrayList<InventoryModel> = ArrayList()
    private var inv_id: Int = 0
    private var inv_title: String = ""
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var gCustomDialog: Dialog
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInventoryDetailsBinding.inflate(layoutInflater)
        inv_id = intent.getIntExtra("inv_id", 0)
        inv_title = intent.getStringExtra("inv_title").toString()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        checkConnectivity()



        val appBarTitle:TextView = findViewById(R.id.inv_title)
        val appBarBackArrow:ImageView = findViewById(R.id.back_icon_o)
        appBarTitle.text = inv_title

        appBarBackArrow.setOnClickListener {
            val intent = Intent(this@InventoryDetails,MainActivity::class.java)
            startActivity(intent)
        }


    }

    private fun checkConnectivity(){
        val connectivity = ConnectivityStatus(this)
        connectivity.observe(this, Observer {
                isConnected ->
            Log.e("isConnected ", "$isConnected")
            if(!isConnected){
                Log.e("False ", "$isConnected")
                binding.progressBarFragment.visibility = View.INVISIBLE
                binding.etSearch.visibility = View.INVISIBLE
                binding.detailListNonInv.visibility = View.INVISIBLE
                binding.siwpeToRefreshFragment.isEnabled = false
                binding.siwpeToRefreshFragment.isRefreshing = false
                binding.tvEnableWifi.visibility = View.VISIBLE
                dataList.clear()
            }
            else{
                Log.e("true ", "$isConnected")
                dataList.clear()
                binding.tvEnableWifi.visibility = View.INVISIBLE
                binding.siwpeToRefreshFragment.isEnabled = true
                binding.siwpeToRefreshFragment.isRefreshing = true
                binding.detailListNonInv.visibility = View.VISIBLE



                binding.apply {
                    createMyRecycle(dataList,inv_title)

                    val scanIcon: ImageView = findViewById(R.id.qr_scanner_o)

                    scanIcon.setOnClickListener {
                        barcodeLauncher.launch(ScanOptions())
                    }

                    val filterIcon: ImageView = findViewById(R.id.filter_icon)

                    filterIcon.setOnClickListener {
                        filterInventoryDialog()
                    }


                    etSearch.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            search(s.toString())
                        }


                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                        }
                    })

                    swipeToRefresh()
                }


                loadInventoryLine(inv_id.toString(), "checkConnectivity")
            }
        })
    }


    private fun swipeToRefresh() {
        binding.siwpeToRefreshFragment.setOnRefreshListener {
            dataList.clear()
            loadInventoryLine(inv_id.toString(), "onCreate swipe To refresh")
        }
    }

    private fun search(text: String) {
        val filteredListSearch = ArrayList<InventoryModel>()
        Log.e("Text", text)
        for (product in dataList) {
            if (product.data.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT)) || product.data.code.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredListSearch.add(product)
            }
        }

        createMyRecycle(filteredListSearch,inv_title)

    }

    private fun filterInventoryDialog() {
        gCustomDialog = Dialog(this@InventoryDetails,R.style.MyDialogStyle)
        val binding: DialogCustumListBinding = DialogCustumListBinding.inflate(layoutInflater)
        gCustomDialog.setContentView(binding.root)

        val inventoryOption = Constants.inventoryOption()
        inventoryOption.add(0, Constants.ALL_OPTIONS)
        binding.rvList.layoutManager = LinearLayoutManager(this@InventoryDetails)
        val adapter = CustomListItemAdapter(
            this@InventoryDetails,
            inventoryOption,
            this@InventoryDetails,
            Constants.FILTER_SELECTION
        )
        binding.rvList.adapter = adapter
        gCustomDialog.show()
    }

    private fun createMyRecycle(listOfProduct: ArrayList<InventoryModel>,inv_title:String) {
        binding.detailListNonInv.layoutManager = LinearLayoutManager(this@InventoryDetails)
        inventoryAdapter = InventoryAdapter(listOfProduct, inv_id,inv_title)
        binding.detailListNonInv.adapter = inventoryAdapter
    }


    private val barcodeLauncher = registerForActivityResult(
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

    private fun textMaker(text: String) {
        GlobalScope.launch {
            async { loadInventoryLineQRCode(text)}.await()
        }
    }


    private fun loadInventoryLineQRCode(asset_code: String) {
        val dataListQR: ArrayList<AssetQRModel> = ArrayList()

        for (asset in dataList) {
            if (asset_code == asset.data.code) {
                Dexter.withContext(this@InventoryDetails)
                    .withPermission(Manifest.permission.INTERNET)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            response?.let {
                                val inventaireApi =
                                    RetrofitHelper.getInstance().create(InventaireApi::class.java)
                                // launching a new coroutine
                                GlobalScope.launch(Dispatchers.Main) {
                                    val result = inventaireApi.getInventaireLineQRCode(asset_code)
                                    Log.e("request body ", result.body().toString())
                                    if (result.body() != null) {
                                        dataListQR.add(result.body()!![0])

                                        Log.e("datalist", dataListQR[0].toString())

                                        val intent = Intent(
                                            this@InventoryDetails,
                                            ProductDetails::class.java
                                        ).apply {
                                            putExtra(Constants.EXIST, "true")
                                            putExtra(Constants.INVENTORY_TITLE, inv_title)
                                            putExtra(Constants.FROM, "QRCODE")
                                            putExtra(Constants.CODE, asset_code)
                                            putExtra(Constants.ETAT, asset.state)
                                            putExtra(Constants.INVENTORY_LINE_ID, asset.id)
                                            putExtra(
                                                Constants.CATEGORY,
                                                dataListQR[0].category_id[1].toString()
                                            )
                                            putExtra(
                                                Constants.DATE_INVENTORY,
                                                LocalDateTime.now(ZoneId.of("GMT+1"))
                                                    .format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")).toString()
                                            )
                                            putExtra(Constants.NAME, dataListQR[0].name)
                                            putExtra(Constants.LOCATION, dataListQR[0].location)
                                            putExtra(Constants.CENTRE_DE_COUT, dataListQR[0].centre_de_cout)
                                            putExtra(Constants.NUMERO_DE_SERIE, dataListQR[0].num_serie)
                                            putExtra(Constants.QUALITY, asset.quality)
                                            putExtra(Constants.QUANTITY, dataListQR[0].quantity.toInt())
                                            putExtra(Constants.ASSET_ID, dataListQR[0].id.toDouble().toString())
                                            putExtra(Constants.INVENTORY_ID, inv_id)
                                            if (dataListQR[0].employee_affected_id.isNotEmpty()) {

                                                putExtra(
                                                    Constants.EMPLOYEE_AFFECTED_TO_ID,
                                                    dataListQR[0].employee_affected_id[0].toString()
                                                )
                                                putExtra(
                                                    Constants.EMPLOYEE_AFFECTED_TO_NAME,
                                                    dataListQR[0].employee_affected_id[1].toString()
                                                )
                                            }
//                putExtra("image", product.image)
                                        }
                                        startActivity(intent)
                                    }

                                }


                            }

                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            if (response!!.isPermanentlyDenied) {
                                AlertDialog.Builder(this@InventoryDetails)
                                    .setMessage(
                                        "Vous avez refuser l'acces a la camera pour utuliser " +
                                                "cette fonctionalit?? veuillez " +
                                                "donner les persmission necessaire a cette aplication"
                                    )
                                    .setPositiveButton("Paramettre") { _, _ ->
                                        run {
                                            try {
                                                val intent =
                                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                val uri =
                                                    Uri.fromParts(
                                                        "package",
                                                        this@InventoryDetails.packageName,
                                                        null
                                                    )
                                                intent.data = uri
                                                startActivity(intent)
                                            } catch (e: ActivityNotFoundException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    .setNegativeButton("Cancel") { dialog, _ ->
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

        Dexter.withContext(this@InventoryDetails)
            .withPermission(Manifest.permission.INTERNET)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    response?.let {
                        val inventaireApi =
                            RetrofitHelper.getInstance().create(InventaireApi::class.java)
                        // launching a new coroutine
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = inventaireApi.getInventaireLineQRCode(asset_code)
                            Log.e("request body ", result.body().toString())
                            if (result.body() != null) {
                                dataListQR.add(result.body()!![0])

                                Log.e("datalist", dataListQR[0].toString())


                                val intent = Intent(
                                    this@InventoryDetails,
                                    ProductDetails::class.java
                                ).apply {
                                    putExtra(Constants.EXIST, "false")
                                    putExtra(Constants.CODE, asset_code)
                                    putExtra(Constants.INVENTORY_TITLE, inv_title)
                                    putExtra(Constants.INVENTORY_LINE_ID, 0)
                                    putExtra(
                                        Constants.CATEGORY,
                                        dataListQR[0].category_id[1].toString()
                                    )
                                    putExtra(
                                        Constants.DATE_INVENTORY,
                                        LocalDateTime.now(ZoneId.of("GMT+1"))
                                            .format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")).toString()
                                    )
                                    putExtra(Constants.NAME, dataListQR[0].name)
                                    putExtra(Constants.LOCATION, dataListQR[0].location)
                                    putExtra(Constants.CENTRE_DE_COUT, dataListQR[0].centre_de_cout)
                                    putExtra(Constants.NUMERO_DE_SERIE, dataListQR[0].num_serie)
                                    putExtra(Constants.QUANTITY, dataListQR[0].quantity.toInt())
                                    putExtra(Constants.ASSET_ID, dataListQR[0].id.toDouble().toString())
                                    putExtra(Constants.QUALITY, "")
                                    putExtra(Constants.ETAT, "draft")
                                    putExtra(Constants.INVENTORY_ID, inv_id)
                                    if (dataListQR[0].employee_affected_id.isNotEmpty()) {

                                        putExtra(
                                            Constants.EMPLOYEE_AFFECTED_TO_ID,
                                            dataListQR[0].employee_affected_id[0].toString()
                                        )
                                        putExtra(
                                            Constants.EMPLOYEE_AFFECTED_TO_NAME,
                                            dataListQR[0].employee_affected_id[1].toString()
                                        )
                                    }
//                putExtra("image", product.image)
                                }
                                startActivity(intent)
                            }

                        }

                    }

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response!!.isPermanentlyDenied) {
                        AlertDialog.Builder(this@InventoryDetails)
                            .setMessage(
                                "Vous avez refuser l'acces a la camera pour utuliser " +
                                        "cette fonctionalit?? veuillez " +
                                        "donner les persmission necessaire a cette aplication"
                            )
                            .setPositiveButton("Paramettre") { _, _ ->
                                run {
                                    try {
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        val uri =
                                            Uri.fromParts(
                                                "package",
                                                this@InventoryDetails.packageName,
                                                null
                                            )
                                        intent.data = uri
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
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

    private fun loadInventoryLine(inv_id: String, from: String) {
        Log.e("loadInventoryLine ", "got to inventory loadInventoryLine from $from")
        val progressBar: ProgressBar = binding.progressBarFragment
        Dexter.withContext(this@InventoryDetails).withPermission(Manifest.permission.INTERNET)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    response?.let {
                        val inventaireApi =
                            RetrofitHelper.getInstance().create(InventaireApi::class.java)
                        // launching a new coroutine
                        GlobalScope.launch(Dispatchers.Main) {
                            try{
                                dataList.clear()
                                binding.tvEnableWifi.visibility = View.INVISIBLE
                                binding.siwpeToRefreshFragment.isEnabled = true
                                binding.siwpeToRefreshFragment.isRefreshing = true
                                binding.detailListNonInv.visibility = View.VISIBLE
                                progressBar.visibility = View.VISIBLE
                                val result = inventaireApi.getInventaireLine(inv_id)
                                Log.e("dataList ",result.body().toString())
                                if (result.body() != null)
//                        Log.e("result ",inv_id)
                                // Checking the results
                                    for (inv in 0 until result.body()!!.size) {

                                        dataList.add(result.body()!![inv])

                                    }
                                progressBar.visibility = View.INVISIBLE
                                inventoryAdapter.notifyDataSetChanged()

                            }catch (e:Exception){

                                binding.progressBarFragment.visibility = View.INVISIBLE
                                binding.etSearch.visibility = View.INVISIBLE
                                binding.detailListNonInv.visibility = View.INVISIBLE
                                binding.siwpeToRefreshFragment.isRefreshing = false
                                binding.tvEnableWifi.visibility = View.VISIBLE
                                dataList.clear()
                                binding.tvEnableWifi.gravity =
                                    Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                                binding.tvEnableWifi.gravity = Gravity.CENTER;
                                binding.tvEnableWifi.text = "Une erreur est survenu veuillez contacter l'administrateur r??seau"
                            }
                        }
                        binding.siwpeToRefreshFragment.isRefreshing = false
                      
                    }


                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response!!.isPermanentlyDenied) {
                        AlertDialog.Builder(this@InventoryDetails)
                            .setMessage(
                                "Vous avez refuser l'acces a la camera pour utuliser " +
                                        "cette fonctionalit?? veuillez " +
                                        "donner les persmission necessaire a cette aplication"
                            )
                            .setPositiveButton("Paramettre") { _, _ ->
                                run {
                                    try {
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        val uri =
                                            Uri.fromParts(
                                                "package",
                                                this@InventoryDetails.packageName,
                                                null
                                            )
                                        intent.data = uri
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
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

    fun filterSelection(filterItemSelection: String) {
        var filterItemSelectionEng = ""
        val filteredLisFilter = ArrayList<InventoryModel>()
        gCustomDialog.dismiss()
        Log.e("Filter Selection ", filterItemSelection)

        if (filterItemSelection == Constants.ALL_OPTIONS) {
            createMyRecycle(dataList,inv_title)
        } else {
            when (filterItemSelection) {
                "Inventori??" -> {
                    filterItemSelectionEng = "done"
                }
                "Non Inventori??" -> {
                    filterItemSelectionEng = "draft"
                }

            }
            for (product in dataList) {
                if (product.state == filterItemSelectionEng) {
                    filteredLisFilter.add(product)
                }
            }
            createMyRecycle(filteredLisFilter,inv_title)
        }
    }

}

