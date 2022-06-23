package com.hasnaoui.bousferimmobilisation

import android.Manifest
import android.R
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.AffectationAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityProductDetailsBinding
import com.hasnaoui.bousferimmobilisation.databinding.ImageViewerBinding
import com.hasnaoui.bousferimmobilisation.models.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


class ProductDetails : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityProductDetailsBinding

    private var dataList: MutableList<AffectationModel> = mutableListOf()
    private var dataAffectation: MutableList<DataAffectation> = mutableListOf()
    var list_of_items = arrayOf("Neuf", "Bon état", "Mauvais état", "Hors service")
    var quality = ""


    private lateinit var postList: PostRequest
    private lateinit var affectationAdapter: AffectationAdapter
    private var i: Int = 1

    private var tempImageUri: Uri? = null
    private var tempImageFilePath = ""
    private var image1Path = ""
    private var image2Path = ""
    private var image3Path = ""

    private var image1 = ""
    private var image2 = ""
    private var image3 = ""
    private var name = ""
    private var inv_title = ""

    private lateinit var gCustomDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#97CBDC"))
        actionBar!!.setBackgroundDrawable(colorDrawable)

        binding.apply {
            spinner.onItemSelectedListener = this@ProductDetails
            // Create an ArrayAdapter using a simple spinner layout and languages array
            val aa = ArrayAdapter(this@ProductDetails, R.layout.simple_spinner_item, list_of_items)
            // Set layout to use when the list of choices appear
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Set Adapter to Spinner
            spinner.adapter = aa
            val exist = intent.getStringExtra("exist")
            val from = intent.getStringExtra("from")
            inv_title = intent.getStringExtra("inv_title").toString()
            val asset_id = intent.getIntExtra("asset_id", 0)
            val inventory_id = intent.getIntExtra("inv_id", 0)
            val id = intent.getIntExtra("inventory_line_id", 0)
            val quantite = intent.getIntExtra("quantite",0)
            val category = intent.getStringExtra("category")
            name = intent.getStringExtra("name").toString()
            val centre_de_cout = intent.getStringExtra("centre_de_cout")
            val location = intent.getStringExtra("location")
            val numSerie = intent.getStringExtra("numSerie")
            quality = intent.getStringExtra("quality")!!
            val dateInventory = intent.getStringExtra("dateInventory")
            val etat = intent.getStringExtra("etat")!!
            val comment = intent.getStringExtra("comment")
            val image = intent.getIntExtra("image", 0)

            tvCategory.text = category?.let { falseToString(it) }
            tvTitle.text = falseToString(name)
            tvCentreDeCout.text = centre_de_cout?.let { falseToString(it) }
            tvLocation.text = location?.let { falseToString(it) }
            tvNumSerie.text = numSerie?.let { falseToString(it) }

            Log.e("inv_title", inv_title)
            if (etat == "draft") {
                tvQuality.isVisible = false
            } else {
                spinner.visibility = View.GONE
                tvQuality.text = quality
            }
            tvDateInventaire.text = dateInventory?.let { falseToString(it) }

            if (etat == "draft") {

                etComment.isFocusable = true;
                etComment.isClickable = true;
                etComment.isEnabled = true
            } else {
                addPicture.visibility = View.GONE
                saveButton.visibility = View.GONE
                etComment.isEnabled = false
                etComment.setText(comment?.let { falseToString(it) })
            }
            if (image != 0) {
                imageTest1.setImageResource(image)
            }

            imageTest1.setOnClickListener {
                filterInventoryDialog(Uri.parse(image1Path))
            }
            imageTest2.setOnClickListener {
                filterInventoryDialog(Uri.parse(image2Path))
            }
            imageTest3.setOnClickListener {
                filterInventoryDialog(Uri.parse(image3Path))
            }
            // Retrieve and cache the system's default "short" animation time.
            addPicture.setOnClickListener {
                Dexter.withContext(this@ProductDetails).withPermission(Manifest.permission.CAMERA)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            response?.let {
                                tempImageUri = FileProvider.getUriForFile(
                                    this@ProductDetails,
                                    "com.hasnaoui.bousferimmobilisation.provider",
                                    createImageFile().also {
                                        tempImageFilePath = it.absolutePath
                                    })

                                cameraLauncher.launch(tempImageUri)
                            }

                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            if (response!!.isPermanentlyDenied) {
                                AlertDialog.Builder(this@ProductDetails)
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
                                                    Uri.fromParts("package", packageName, null)
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

            saveButton.setOnClickListener {
                binding.constraintLayout.alpha = 0.4F
                if (exist.toBoolean()) {
                    saveIfExistOrNot(
                        id,
                        etComment.text.toString(),
                        asset_id,
                        inventory_id,
                        tvDateInventaire.text.toString(),
                        true
                    )
                } else {
                    saveIfExistOrNot(
                        id,
                        etComment.text.toString(),
                        asset_id,
                        inventory_id,
                        tvDateInventaire.text.toString(),
                        false
                    )
                }

            }

            affectationAdapter = AffectationAdapter(dataList, etat)
            rvAffectedTo.apply {
                layoutManager = LinearLayoutManager(this@ProductDetails)
                adapter = affectationAdapter

                if (from == "click") {
                    getCheckListAndPopulateRV(id)
                }
                else{
                    progressBarDetail.visibility = View.INVISIBLE
                    if(location != "" && location!=null){
                    dataList.add(AffectationModel(true,"",0,"Localisé à: $location"))
                    }
                    dataList.add(AffectationModel(true,"",0,"Quantité attendue: $quantite"))
                }

            }

        }

    }

    private fun filterInventoryDialog(image: Uri) {
        gCustomDialog = Dialog(this@ProductDetails)
        val binding: ImageViewerBinding = ImageViewerBinding.inflate(layoutInflater)
        gCustomDialog.setContentView(binding.root)
        binding.resizedImage.setImageURI(image)
        gCustomDialog.show()
    }


    private fun saveIfExistOrNot(
        idA: Int,
        etCommentA: String,
        asset_idA: Int,
        inventory_idA: Int,
        tvDateInventaireA: String,
        exist: Boolean
    ) {
        val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
        val progressBar: ProgressBar = binding.progressSave
        // launching a new coroutine
        GlobalScope.launch(Dispatchers.Main) {
            if (dataList.isNotEmpty()) {
                for (i in 0 until dataList.size) {
                    dataAffectation.add(
                        DataAffectation(
                            dataList[i].name,
                            dataList[i].comment.toString(),
                            dataList[i].id,
                            dataList[i].checked.toString().toBoolean()
                        )
                    )
                }
            }

            val post = PostRequest(
                idA,
                name,
                etCommentA,
                switchQuality(binding.spinner.selectedItem.toString()),
                asset_idA,
                "done",
                inventory_idA,
                tvDateInventaireA,
                image1,
                image2,
                image3,
                dataAffectation
            )
            progressBar.visibility = View.VISIBLE
            if (exist) {
                Log.e("Post ", exist.toString())
                inventaireApi.saveAssetAssetLine(post)
            } else {
                Log.e("Post ", exist.toString())
                inventaireApi.saveAssetAssetLineExistNot(post)
            }
            dataAffectation.clear()
            progressBar.visibility = View.INVISIBLE
            val intent = Intent(this@ProductDetails, InventoryDetails::class.java).apply {
                putExtra("inv_id", inventory_idA)
                putExtra("inv_title", inv_title)
            }
            startActivity(intent)
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                if (i <= 3) {
                    Log.e("i ", i.toString())
                    when (i) {
                        1 -> {
                            binding.imageTest1.setImageURI(tempImageUri)
                            i += 1
                            image1Path = tempImageFilePath
                            image1 = encodeToBase64(image1Path)
                        }

                        2 -> {
                            binding.imageTest2.setImageURI(tempImageUri)
                            i += 1
                            image2Path = tempImageFilePath
                            image2 = encodeToBase64(image2Path)
                        }
                        3 -> {
                            binding.imageTest3.setImageURI(tempImageUri)
                            i = 1
                            image3Path = tempImageFilePath
                            image3 = encodeToBase64(image3Path)
                        }
                    }

                }
//                Log.e("i ",i.toString())
            }
        }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image", ".jpg", storageDir)
    }

    private fun getCheckListAndPopulateRV(inventoryLineId: Int) {
        val progressBar: ProgressBar = binding.progressBarDetail
        Dexter.withContext(this@ProductDetails).withPermission(Manifest.permission.INTERNET)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    response?.let {
                        val inventaireApi =
                            RetrofitHelper.getInstance().create(InventaireApi::class.java)
                        // launching a new coroutine
                        GlobalScope.launch(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                            val result = inventaireApi.getCheckList(
                                inventoryLineId.toString()
                            )
                            Log.e("CheckList", result.body().toString())
                            Log.e("CheckList", inventoryLineId.toString())
                            if (result.body() != null && result.body()!!.size != 0) {
                                for (affect in 0 until result.body()!!.size) {

                                    dataList.add(result.body()!![affect])
                                }
                            }

                            progressBar.visibility = View.INVISIBLE
                            affectationAdapter.notifyDataSetChanged()
                        }


                    }


//

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response!!.isPermanentlyDenied) {
                        AlertDialog.Builder(this@ProductDetails)
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
                                            Uri.fromParts("package", packageName, null)
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

    private fun falseToString(_value: String): String {
        println("---------------------------$_value")
        val value = ""
        if (_value == "false")
            return value
        return _value
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        quality = list_of_items[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun encodeToBase64(imagePath: String): String {
        return if (imagePath != "") {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeFile(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } else ""

    }

    private fun switchQuality(quality: String): String {
        var qualityFormatted = ""
        when (quality) {
            "Neuf" -> {
                qualityFormatted = "new"
            }
            "Bon état" -> {
                qualityFormatted = "good"
            }
            "Mauvais état" -> {
                qualityFormatted = "bad"
            }
            "Hors service" -> {
                qualityFormatted = "breakdown"
            }
        }
        return qualityFormatted
    }
}