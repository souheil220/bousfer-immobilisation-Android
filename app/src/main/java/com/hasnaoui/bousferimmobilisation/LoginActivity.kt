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
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnaoui.bousferimmobilisation.adapters.SampleAdapter
import com.hasnaoui.bousferimmobilisation.databinding.ActivityLoginBinding
import com.hasnaoui.bousferimmobilisation.models.LoginModel
import com.hasnaoui.bousferimmobilisation.models.LoginModelResponse
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

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
                binding.cardViewUrl.visibility = View.INVISIBLE
                binding.cardViewUsername.visibility = View.INVISIBLE
                binding.cardViewPassword.visibility = View.INVISIBLE
                binding.loginButton.visibility = View.INVISIBLE
                binding.tvEnableWifi.visibility = View.VISIBLE
            }
            else{
                binding.tvEnableWifi.visibility = View.INVISIBLE
                binding.cardViewUrl.visibility = View.VISIBLE
                binding.cardViewUsername.visibility = View.VISIBLE
                binding.cardViewPassword.visibility = View.VISIBLE
                binding.loginButton.visibility = View.VISIBLE
                Log.e("true ", "$isConnected")
                binding.loginButton.setOnClickListener {
                    Log.e("eeeeeeeee","llllllllllll")
                    binding.progressBar.visibility = View.VISIBLE
                    Dexter.withContext(this@LoginActivity).withPermission(Manifest.permission.INTERNET).
                    withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            response?.let {
                                var responset: LoginModelResponse? = null
                                val inventaireApi = RetrofitHelper.getInstance().create(InventaireApi::class.java)
                                // launching a new coroutine
                                GlobalScope.launch (Dispatchers.Main){
//                            progressBar.visibility = View.VISIBLE
                                    val result = inventaireApi.login(LoginModel(binding.etUrl.text.toString(),binding.etUsername.text.toString(),binding.etPassword.text.toString()))
                                    if (result.body() != null)
                                    // Checking the results
                                        responset = result.body()

                                    if(responset!!.message!="False"){
                                        binding.progressBar.visibility = View.INVISIBLE
                                        Log.e("result","true")
                                        val sessionManagement = SessionManagement(this@LoginActivity)
                                        Log.e("ID",responset!!.message)
                                        sessionManagement.saveSession(responset!!.message.toInt())
                                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    }
                                    else{
                                        binding.progressBar.visibility = View.INVISIBLE
                                        Toast.makeText(this@LoginActivity, "Un Probleme est survenu", Toast.LENGTH_SHORT).show()
                                        Log.e("result",responset!!.message)
                                    }
//                            progressBar.visibility = View.INVISIBLE

                                }

                            }


//

                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            if (response!!.isPermanentlyDenied) {
                                AlertDialog.Builder(this@LoginActivity)
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
        }
        )
    }


    override fun onStart() {
        super.onStart()
        val sessionManagement:SessionManagement = SessionManagement(this@LoginActivity)
        val isUserLoggedIn = sessionManagement.getSession()
        if(isUserLoggedIn){
            moveToMainActivity()
        }
    }

    fun moveToMainActivity(){
        Log.e("move","moooooove")
        val intent = Intent(this@LoginActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}