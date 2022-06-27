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
import com.google.zxing.datamatrix.encoder.DefaultPlacement
import com.hasnaoui.bousferimmobilisation.adapters.AffectationAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityProductDetailsBinding
import com.hasnaoui.bousferimmobilisation.databinding.ImageViewerBinding
import com.hasnaoui.bousferimmobilisation.models.*
import com.hasnaoui.bousferimmobilisation.utils.Constants
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
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
    private var code =""
    private var asset_id = 0

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
            val exist = intent.getStringExtra(Constants.EXIST)
            val from = intent.getStringExtra(Constants.FROM)
            code = intent.getStringExtra(Constants.CODE).toString()
            inv_title = intent.getStringExtra(Constants.INVENTORY_TITLE).toString()
            asset_id = intent.getStringExtra(Constants.ASSET_ID)!!.toDouble().toInt()
            val inventory_id = intent.getIntExtra(Constants.INVENTORY_ID, 0)
            val id = intent.getIntExtra(Constants.INVENTORY_LINE_ID, 0)
            val quantite = intent.getIntExtra(Constants.QUANTITY,0)
            val category = intent.getStringExtra(Constants.CATEGORY)
            name = intent.getStringExtra(Constants.NAME).toString()
            val centre_de_cout = intent.getStringExtra(Constants.CENTRE_DE_COUT)
            val location = intent.getStringExtra(Constants.LOCATION)
            val numSerie = intent.getStringExtra(Constants.NUMERO_DE_SERIE)
            quality = intent.getStringExtra(Constants.QUALITY)!!
            val dateInventory = intent.getStringExtra(Constants.DATE_INVENTORY)
            val etat = intent.getStringExtra(Constants.ETAT)!!
            val comment = intent.getStringExtra(Constants.COMMENT)
            val image = intent.getIntExtra(Constants.IMAGE, 0)

            tvCategory.text = category?.let { falseToString(it) }
            tvTitle.text = falseToString(name)
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

            setImageUsingPicasso("image_produit",code.replace("/",""),asset_id.toString(),imageAsset1,1)
            setImageUsingPicasso("image_produit",code.replace("/",""),asset_id.toString(),imageAsset2,2)
            setImageUsingPicasso("image_produit",code.replace("/",""),asset_id.toString(),imageAsset3,3)

            setImageUsingPicasso("image_inventory",code.replace("/",""),asset_id.toString(),imageInventaire1,1)
            setImageUsingPicasso("image_inventory",code.replace("/",""),asset_id.toString(),imageInventaire2,2)
            setImageUsingPicasso("image_inventory",code.replace("/",""),asset_id.toString(),imageInventaire3,3)



            imageAsset1.setOnClickListener {
                filterInventoryDialog(Uri.parse(""),"image_produit","",1)
            }
            imageAsset2.setOnClickListener {
                filterInventoryDialog(Uri.parse(""),"image_produit","",2)
            }
            imageAsset3.setOnClickListener {
                filterInventoryDialog(Uri.parse(""),"image_produit","",3)
            }
            imageInventaire1.setOnClickListener {
                filterInventoryDialog(Uri.parse(image1Path),"image_inventory",etat,1)
            }
            imageInventaire2.setOnClickListener {
                filterInventoryDialog(Uri.parse(image2Path),"image_inventory",etat,2)
            }
            imageInventaire3.setOnClickListener {
                filterInventoryDialog(Uri.parse(image3Path),"image_inventory",etat,3)
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

    private fun setImageUsingPicasso(folder:String,code:String,asset_id:String,placement: ImageView,number:Int){
        Picasso.get().load("${Constants.BASE_URL}/images/$folder/${code.replace("/","")}/${asset_id}image$number.jpg").fit().centerCrop()
            .into(placement);
    }

    private fun filterInventoryDialog(image: Uri,folder:String,etat:String,number: Int) {
        val binding: ImageViewerBinding = ImageViewerBinding.inflate(layoutInflater)
        gCustomDialog = Dialog(this@ProductDetails)
        gCustomDialog.setContentView(binding.root)
        if(etat == "draft"){
        binding.resizedImage.setImageURI(image)
        }
        else{
            Picasso.get().load("${Constants.BASE_URL}/images/$folder/${code.replace("/","")}/${asset_id}image$number.jpg").fit().centerCrop()
                .into(binding.resizedImage);
        }
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
                code,
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
                putExtra(Constants.INVENTORY_ID, inventory_idA)
                putExtra(Constants.INVENTORY_TITLE, inv_title)
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
                            binding.imageInventaire1.setImageURI(tempImageUri)
                            i += 1
                            image1Path = tempImageFilePath
                            image1 = encodeToBase64(image1Path)
                        }

                        2 -> {
                            binding.imageInventaire2.setImageURI(tempImageUri)
                            i += 1
                            image2Path = tempImageFilePath
                            image2 = encodeToBase64(image2Path)
                        }
                        3 -> {
                            binding.imageAsset3.setImageURI(tempImageUri)
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