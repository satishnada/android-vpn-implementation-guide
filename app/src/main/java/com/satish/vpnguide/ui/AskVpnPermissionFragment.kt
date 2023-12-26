package com.satish.vpnguide.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.satish.vpnguide.Logger
import com.satish.vpnguide.R

class AskVpnPermissionFragment : BottomSheetFragment() {

    private val log = Logger("AskVpnPermissionFragment")
    lateinit var onVpnRequestPermission: OnVpnRequestPermission

    companion object {
        fun newInstance() = AskVpnPermissionFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_vpnprofile, container, false)
        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            log.e("Ask permission dialog back")
            dismiss()
        }

        val vpnContinue: View = root.findViewById(R.id.vpnperm_continue)
        vpnContinue.setOnClickListener {
            log.e("VPN permission continue clicked")
            onVpnRequestPermission.onRequestVpn()
            dismiss()
        }

        return root
    }

    fun setOnPermissionRequest(onRequest: OnVpnRequestPermission) {
        onVpnRequestPermission = onRequest
    }

}