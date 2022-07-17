package com.hasnaoui.bousferimmobilisation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.InventoryAdapter
import com.hasnaoui.bousferimmobilisation.adapters.SampleAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityMainBinding
import com.hasnaoui.bousferimmobilisation.models.InventoryModel
import com.hasnaoui.bousferimmobilisation.models.SampleModel
import com.hasnaoui.bousferimmobilisation.utils.SessionManagement
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var toggle:ActionBarDrawerToggle
    private var userID=0

    private var dataList:MutableList<SampleModel> = mutableListOf()
    private lateinit var sampleAdapter: SampleAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkConnectivity()

    }

    private fun checkConnectivity(){
        val connectivity = ConnectivityStatus(this)
        connectivity.observe(this, Observer {
                isConnected ->
            Log.e("isConnected ", "$isConnected")
            if(!isConnected){
                Log.e("False ", "$isConnected")
                binding.progressBar.visibility = View.INVISIBLE
                binding.textInputLayout2.visibility = View.INVISIBLE
                binding.invList.visibility = View.INVISIBLE
                binding.siwpeToRefresh.isEnabled = false
                binding.siwpeToRefresh.isRefreshing = false
                binding.tvEnableWifi.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                dataList.clear()
            }
            else{
                Log.e("true ", "$isConnected")
                dataList.clear()
                binding.tvEnableWifi.visibility = View.INVISIBLE
                binding.siwpeToRefresh.isEnabled = true
                binding.siwpeToRefresh.isRefreshing = true
                binding.invList.visibility = View.VISIBLE
                binding.textInputLayout2.visibility = View.VISIBLE
                binding.apply {

                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    toggle = ActionBarDrawerToggle(this@MainActivity,drawerLayout,R.string.open,R.string.close)
                    toggle.drawerArrowDrawable.color = resources.getColor(R.color.black)
                    drawerLayout.addDrawerListener(toggle)
                    toggle.syncState()

                    val colorDrawable = ColorDrawable(Color.parseColor("#FFFFFF"))

                    // Set BackgroundDrawable

                    // Set BackgroundDrawable
                    val actionBar: ActionBar? = supportActionBar

                    actionBar!!.setBackgroundDrawable(colorDrawable)
                    actionBar.title = Html.fromHtml("<font color=\"#006ABD\">"+getString(R.string.app_name)+"</font>")

                    supportActionBar?.setDisplayHomeAsUpEnabled(true)

                    navView.setNavigationItemSelectedListener {
                        when(it.itemId){
                            R.id.profile->{
                                Toast.makeText(this@MainActivity, "Profile", Toast.LENGTH_SHORT).show()
                            }
                            R.id.sign_out->{
                                Log.e("sign out","signout")
                                val sessionManagement: SessionManagement = SessionManagement(this@MainActivity)
                                userID = sessionManagement.getIDFromSession()
                                val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                                // launching a new coroutine
                                GlobalScope.launch (Dispatchers.Main){
                                    progressBar.visibility = View.VISIBLE
                                    Log.e("ID",sessionManagement.getIDFromSession().toString())
                                    inventaireApi.getLogout(userID)

                                }
                                sessionManagement.logOutUser()
                                val intent = Intent(this@MainActivity,LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                        true
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


                    createMyRecycle(dataList)
                }

                refreshApp()

                loadPartsAndUpdateList()
            }
        })
    }

    private fun createMyRecycle(listOfProduct: MutableList<SampleModel>) {
        sampleAdapter = SampleAdapter(listOfProduct)
        binding.invList. apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter= sampleAdapter
//            loadPartsAndUpdateList()
        }
    }

    private fun search(text: String) {
        val filteredListSearch = ArrayList<SampleModel>()
        Log.e("Text", text)
        for (product in dataList) {
            if (product.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredListSearch.add(product)
            }
        }

        createMyRecycle(filteredListSearch)

    }



    private fun refreshApp(){

        binding.siwpeToRefresh.setOnRefreshListener {
            dataList.clear()
            try {
                loadPartsAndUpdateList()
            }catch (e:Exception){
                binding.constraintLayout.visibility = View.GONE
            }

        }
    }

    override fun onStart() {
        super.onStart()
        val sessionManagement:SessionManagement = SessionManagement(this@MainActivity)
        userID = sessionManagement.getIDFromSession()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPartsAndUpdateList() {
        Log.e("refresh ","refreshed")
        val progressBar:ProgressBar = binding.progressBar
        Dexter.withContext(this@MainActivity).withPermission(Manifest.permission.INTERNET).
        withListener(object : PermissionListener{
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                response?.let {

                    val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                    // launching a new coroutine
                    GlobalScope.launch (Dispatchers.Main){
                        try {
                            dataList.clear()
                            binding.tvEnableWifi.visibility = View.INVISIBLE
                            binding.siwpeToRefresh.isEnabled = true
                            binding.invList.visibility = View.VISIBLE
                            binding.textInputLayout2.visibility = View.VISIBLE
                            progressBar.visibility = View.VISIBLE
                            val result = inventaireApi.getInventaire(userID)
                            if (result.body() != null)
                            // Checking the results
                                for (inv in 0 until  result.body()!!.size){
                                    dataList.add(result.body()!![inv])
                                }
                            progressBar.visibility = View.INVISIBLE
                            sampleAdapter.notifyDataSetChanged()

                        }
                        catch (e:Exception){
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.etSearch.visibility = View.INVISIBLE
                            binding.invList.visibility = View.INVISIBLE
                            binding.siwpeToRefresh.isRefreshing = false
                            binding.tvEnableWifi.visibility = View.VISIBLE
                            dataList.clear()
                            binding.tvEnableWifi.gravity =
                                Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                            binding.tvEnableWifi.gravity = Gravity.CENTER;
                            binding.tvEnableWifi.text = "Une erreur est survenu veuillez contacter l'administrateur réseau"
                        }

                    }

                    binding.siwpeToRefresh.isRefreshing = false


                }


//

            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response!!.isPermanentlyDenied) {
                    AlertDialog.Builder(this@MainActivity)
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