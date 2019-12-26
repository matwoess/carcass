package com.mathias.android.carcass.sheets

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mathias.android.carcass.FireDBHelper
import com.mathias.android.carcass.IBottomSheetAnimalTypeListener
import com.mathias.android.carcass.R


class BottomSheetAnimalType : BottomSheetDialogFragment() {

    private lateinit var mTxtName: EditText
    private lateinit var mTxtHint: TextView
    private lateinit var mBtnSave: Button

    private var mClickListener: IBottomSheetAnimalTypeListener? = null

    fun newInstance(): BottomSheetAnimalType {
        return BottomSheetAnimalType()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_animal_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTxtName = view.findViewById(R.id.txt_dialog_animal_type_name)
        mTxtHint = view.findViewById(R.id.txt_dialog_animal_type_hint)
        mBtnSave = view.findViewById(R.id.btn_save_animal_type)

        mTxtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val duplicates: Boolean = FireDBHelper.animalTypes.values.stream()
                    .anyMatch { t -> t.name.contentEquals(charSequence) }
                mBtnSave.isEnabled = !duplicates
                mTxtHint.visibility = if (duplicates) View.VISIBLE else View.INVISIBLE
                mBtnSave.setOnClickListener {
                    val name = mTxtName.text.toString()
                    mClickListener!!.onAnimalTypeSaved(name)
                    dismiss()
                }
            }
        })
        if (mTxtName.requestFocus()) {
            val imm: InputMethodManager? =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(mTxtName, 0)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mClickListener = context as IBottomSheetAnimalTypeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BottomSheetListener")
        }

    }
}