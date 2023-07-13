

package com.satish.vpnguide.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.satish.vpnguide.R
import com.satish.vpnguide.service.VpnPermissionService
import service.VpnPermissionService
import ui.BottomSheetFragment
import ui.TunnelViewModel
import ui.app

class AskVpnProfileFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = AskVpnProfileFragment()
    }

    private val vpnPerm = VpnPermissionService
    private lateinit var vm: TunnelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_vpnprofile, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val vpnContinue: View = root.findViewById(R.id.vpnperm_continue)
        vpnContinue.setOnClickListener {
            vpnPerm.askPermission()
        }

        vpnPerm.onPermissionGranted = { granted ->
            if (granted) {
                vm.turnOn()
                dismiss()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vpnPerm.onPermissionGranted = {}
    }

}