package com.satish.vpnguide.ui

import android.os.Bundle
import android.view.*
import com.satish.vpnguide.R

class FirstTimeFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = FirstTimeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_firsttime, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val firstTimeContinue: View = root.findViewById(R.id.firsttime_continue)
        firstTimeContinue.setOnClickListener {
            dismiss()
        }

        return root
    }

}