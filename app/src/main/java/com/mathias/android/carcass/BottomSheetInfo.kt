package com.mathias.android.carcass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetInfo : BottomSheetDialogFragment() {

    private lateinit var mTxtType: TextView
    private lateinit var mTxtDescription: TextView
    private lateinit var mTxtReported: TextView
    private lateinit var mTxtLocation: TextView
    private lateinit var mBtnShowPicture: Button
    private lateinit var mBtnEdit: Button
    private lateinit var mBtnRemove: Button
    private lateinit var mBtnReport: Button

    private lateinit var mCarcass: Carcass

    internal fun newInstance(carcass: Carcass): BottomSheetInfo {
        return BottomSheetInfo().apply { mCarcass = carcass }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sheet_info, container, false)

        /*mTxtType = view.findViewById(R.id.)
        mTxtDescription = view.findViewById(R.id.txt_info_sheet_from)
        mTxtReported = view.findViewById(R.id.txt_create_sheet_to)
        mTxtLocation = view.findViewById(R.id.txt_create_sheet_to)
        mBtnShowPicture = view.findViewById(R.id.btn_info_sheet_edit_route)
        mBtnRemove = view.findViewById(R.id.btn_info_sheet_edit_route)
        mBtnReport = view.findViewById(R.id.btn_info_sheet_edit_route)*/
        return view
    }
}