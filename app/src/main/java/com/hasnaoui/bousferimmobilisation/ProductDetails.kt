package com.hasnaoui.bousferimmobilisation

import android.Manifest
import android.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.AffectationAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityProductDetailsBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            spinner.setOnItemSelectedListener(this@ProductDetails)
            // Create an ArrayAdapter using a simple spinner layout and languages array
            val aa = ArrayAdapter(this@ProductDetails, R.layout.simple_spinner_item, list_of_items)
            // Set layout to use when the list of choices appear
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Set Adapter to Spinner
            spinner.adapter = aa
            val from = intent.getStringExtra("from")
            val asset_id = intent.getStringExtra("asset_id")!!.toDouble().toInt()
            val inventory_id = intent.getIntExtra("inv_id", 0)
            val id = intent.getIntExtra("id", 0)
            val category = intent.getStringExtra("category")
            val name = intent.getStringExtra("name")
            val centre_de_cout = intent.getStringExtra("centre_de_cout")
            val location = intent.getStringExtra("location")
            val numSerie = intent.getStringExtra("numSerie")
            quality = intent.getStringExtra("quality")!!
            val dateInventory = intent.getStringExtra("dateInventory")
            val etat = intent.getStringExtra("etat")!!
            val comment = intent.getStringExtra("comment")
            val image = intent.getIntExtra("image", 0)

            tvCategory.text = category?.let { falseToString(it) }
            tvTitle.text = name?.let { falseToString(it) }
            tvCentreDeCout.text = centre_de_cout?.let { falseToString(it) }
            tvLocation.text = location?.let { falseToString(it) }
            tvNumSerie.text = numSerie?.let { falseToString(it) }
            if (etat == "draft") {
                tvQuality.isVisible = false
            } else {
                spinner.visibility = View.GONE
                tvQuality.text = quality
            }
            tvDateInventaire.text = dateInventory?.let { falseToString(it) }

            if (etat == "draft" || from == "QRCODE") {

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
                val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
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
                        etComment.text.toString(),
                        switchQuality(binding.spinner.selectedItem.toString()),
                        asset_id,
                        "done",
                        inventory_id,
                        tvDateInventaire.text.toString(),
                        image1,
                        image2,
                        image3,
                        dataAffectation
                    )

                    inventaireApi.saveAssetAssetLine(post)

                    dataAffectation.clear()
                    val intent = Intent(this@ProductDetails, MainActivity::class.java).apply{
                        putExtra("inv_id",inventory_id)
                    }
                    startActivity(intent)
                }
            }

            affectationAdapter = AffectationAdapter(dataList, etat)
            rvAffectedTo.apply {
                layoutManager = LinearLayoutManager(this@ProductDetails)
                adapter = affectationAdapter


                loadPartsAndUpdateList(id)

            }

        }

    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                if (i <= 3) {
                    Log.e("i ",i.toString())
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

    private fun loadPartsAndUpdateList(userId: Int) {
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
                                userId.toString()
                            )

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
        return if (imagePath!=""){
            val byteArrayOutputStream = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeFile(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } else ""

    }

    private fun switchQuality(quality:String):String{
        var qualityFormatted=""
          when(quality){
                "Neuf" -> {qualityFormatted = "new"}
                "Bon état" -> {qualityFormatted = "good"}
                "Mauvais état" -> {qualityFormatted = "bad"}
                "Hors service" -> {qualityFormatted = "breakdown"}
        }
        return qualityFormatted
    }
}