package com.mathias.android.carcass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetInfo : BottomSheetDialogFragment() {

    private lateinit var txtType: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtReported: TextView
    private lateinit var txtLocation: TextView
    private lateinit var btnShowPicture: Button
    private lateinit var btnEdit: Button
    private lateinit var btnRemove: Button
    private lateinit var btnReport: Button

    private lateinit var carcass: Carcass

    internal fun newInstance(carcass: Carcass): BottomSheetInfo {
        return BottomSheetInfo().apply { this.carcass = carcass }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtType = view.findViewById(R.id.txt_type)
        txtDescription = view.findViewById(R.id.txt_description)
        txtReported = view.findViewById(R.id.txt_reported)
        txtLocation = view.findViewById(R.id.txt_location)
        btnShowPicture = view.findViewById(R.id.btn_show_picture)
        btnRemove = view.findViewById(R.id.btn_remove)
        btnReport = view.findViewById(R.id.btn_remove)
        btnEdit = view.findViewById(R.id.btn_edit)
        txtType.text = carcass.type?.name
        txtDescription.text = carcass.description
        txtReported.text = carcass.reportedat.toString()
        txtLocation.text = carcass.location.toString()
    }
}