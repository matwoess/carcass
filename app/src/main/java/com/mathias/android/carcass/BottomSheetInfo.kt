package com.mathias.android.carcass

import android.app.Dialog
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mathias.android.carcass.ActivityMaps.Companion.EDIT_REQUEST_CODE
import com.mathias.android.carcass.ActivityMaps.Companion.fireDBHandler
import com.mathias.android.carcass.ActivityMaps.Companion.geocoder
import com.mathias.android.carcass.AddActivity.Companion.EXISTING_KEY
import com.mathias.android.carcass.FireDBHelper.Companion.carcasses
import com.mathias.android.carcass.model.Carcass
import java.text.SimpleDateFormat


class BottomSheetInfo : BottomSheetDialogFragment() {
    private lateinit var txtType: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtReported: TextView
    private lateinit var txtLocation: TextView
    private lateinit var btnShowPicture: Button
    private lateinit var btnEdit: Button
    private lateinit var btnRemove: Button
    private lateinit var btnReport: Button

    private lateinit var key: String
    private lateinit var carcass: Carcass

    internal fun newInstance(key: String): BottomSheetInfo {
        return BottomSheetInfo().apply {
            this.key = key
            this.carcass = carcasses[key]!!
        }
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
        initUI(view)
        initButtons(view)
    }

    private fun initUI(view: View) {
        txtType = view.findViewById(R.id.txt_type)
        txtDescription = view.findViewById(R.id.txt_description)
        txtReported = view.findViewById(R.id.txt_reported)
        txtLocation = view.findViewById(R.id.txt_location)
        btnShowPicture = view.findViewById(R.id.btn_show_picture)
        btnRemove = view.findViewById(R.id.btn_remove)
        btnReport = view.findViewById(R.id.btn_report)
        btnEdit = view.findViewById(R.id.btn_edit)
        txtType.text = carcass.type?.name
        txtDescription.text = carcass.description
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm");
        txtReported.text = dateFormat.format(carcass.reportedAt!!)
        Log.i(TAG, geocoder.toString())
        val addresses: List<Address> = geocoder.getFromLocation(
            carcass.location.lat,
            carcass.location.lng,
            1
        )
        txtLocation.text = if (addresses.isNotEmpty()) addresses[0].thoroughfare else "N/A"
    }

    private fun initButtons(view: View) {
        btnShowPicture.setOnClickListener { showPicture(view) }
        btnRemove.setOnClickListener { deleteCarcass() }
        btnEdit.setOnClickListener { editCarcass() }
    }

    private fun editCarcass() {
        val intent = Intent(activity, AddActivity::class.java).apply {
            putExtra(EXISTING_KEY, key)
        }
        this.dismiss()
        activity?.startActivityForResult(intent, EDIT_REQUEST_CODE)
    }

    private fun deleteCarcass() {
        fireDBHandler.removeCarcass(carcass)
        this.dismiss()
    }

    private fun showPicture(view: View) {
        val settingsDialog = Dialog(view.context)
        settingsDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        settingsDialog.setContentView(
            layoutInflater.inflate(
                R.layout.image_view
                , null
            )
        )
        settingsDialog.show()
    }

    companion object {
        private const val TAG = "BottomSheetInfo"
    }
}