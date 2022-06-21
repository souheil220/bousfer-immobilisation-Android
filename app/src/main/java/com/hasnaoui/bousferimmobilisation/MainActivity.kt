package com.hasnaoui.bousferimmobilisation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.SampleAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityMainBinding
import com.hasnaoui.bousferimmobilisation.models.SampleModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var toggle:ActionBarDrawerToggle

    private var dataList:MutableList<SampleModel> = mutableListOf()
    private lateinit var sampleAdapter: SampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)




        binding.apply {


            toggle = ActionBarDrawerToggle(this@MainActivity,drawerLayout,R.string.open,R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            val colorDrawable = ColorDrawable(Color.parseColor("#97CBDC"))

            // Set BackgroundDrawable

            // Set BackgroundDrawable
            val actionBar: ActionBar? = supportActionBar

            actionBar!!.setBackgroundDrawable(colorDrawable)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            navView.setNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.profile->{
                        Toast.makeText(this@MainActivity, "Profile", Toast.LENGTH_SHORT).show()
                    }
                    R.id.sign_out->{
                        Toast.makeText(this@MainActivity, "Sign out", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

//            loadData()


            sampleAdapter = SampleAdapter(dataList)
            invList. apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter= sampleAdapter
                loadPartsAndUpdateList()
            }
        }

        refreshApp()
    }

    private fun refreshApp(){
        binding.siwpeToRefresh.setOnRefreshListener {
            dataList.clear()
            loadPartsAndUpdateList()

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
           return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPartsAndUpdateList() {
        val progressBar:ProgressBar = binding.progressBar
        Dexter.withContext(this@MainActivity).withPermission(Manifest.permission.INTERNET).
        withListener(object : PermissionListener{
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                response?.let {

                    val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                    // launching a new coroutine
                    GlobalScope.launch (Dispatchers.Main){
                        progressBar.visibility = View.VISIBLE
                        val result = inventaireApi.getInventaire()
                        if (result.body() != null)
                        // Checking the results
                            for (inv in 0 until  result.body()!!.size){
                                dataList.add(result.body()!![inv])
                            }
                        progressBar.visibility = View.INVISIBLE
                        sampleAdapter.notifyDataSetChanged()
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