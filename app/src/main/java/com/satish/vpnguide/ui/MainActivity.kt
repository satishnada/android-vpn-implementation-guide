package com.satish.vpnguide.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.satish.vpnguide.Logger
import com.satish.vpnguide.databinding.ActivityMainBinding
import com.satish.vpnguide.service.ContextService
import com.satish.vpnguide.service.LocalVpnService

class MainActivity : AppCompatActivity() {

    private val log = Logger("MainActivity")
    private lateinit var binding: ActivityMainBinding
    private lateinit var prepareVpnLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ContextService.setActivityContext(this)
        ContextService.setAppContext(this)

        // Initialize the launcher
        prepareVpnLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle the result here
                    Log.i("TAG", "onRequestPermissionsResult: ")
                    startVpnService()
                }
            }

        binding.btnVpn.setOnClickListener {
            val fragment = AskVpnPermissionFragment.newInstance()
            fragment.setOnPermissionRequest(object : OnVpnRequestPermission {
                override fun onRequestVpn() {
                    if (LocalVpnService.isVpnRunning) {
                        stopVpnService()
                    } else {
                        if (hasPermission()) {
                            startVpnService()
                        } else {
                            askPermission()
                        }
                    }

                }
            })
            fragment.show(supportFragmentManager, null)
        }

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if (LocalVpnService.isVpnRunning) {
            binding.btnVpn.text = "Stop VPN"
            binding.homeStatus.text = "Activated"
        } else {
            binding.btnVpn.text = "Start VPN"
            binding.homeStatus.text = "Deactivated"
        }
    }

    fun startVpnService() {
        binding.btnVpn.text = "Stop VPN"
        binding.homeStatus.text = "Activated"
        // Start the Local VPN service
        val vpnIntent = Intent(this, LocalVpnService::class.java).apply {
            action = "START_VPN_SERVICE"
        }
        startService(vpnIntent)
        val fragment = FirstTimeFragment.newInstance()
        fragment.show(supportFragmentManager, null)
    }

    fun stopVpnService() {
        binding.btnVpn.text = "Start VPN"
        binding.homeStatus.text = "Deactivated"
        // Start the Local VPN service
        val vpnIntent = Intent(this, LocalVpnService::class.java).apply {
            action = "STOP_VPN_SERVICE"
        }
        startService(vpnIntent)
    }

    fun askPermission() {
        log.w("Asking for VPN permission")

        val intent = VpnService.prepare(this)
        intent?.let {
            prepareVpnLauncher.launch(it)
        }
    }

    fun hasPermission(): Boolean {
        return VpnService.prepare(this) == null
    }

}