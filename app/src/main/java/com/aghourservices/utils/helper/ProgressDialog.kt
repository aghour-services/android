package com.aghourservices.utils.helper

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.aghourservices.R

class ProgressDialog(context: Context) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
    }

    fun show(message: String) {
        val loadingMessage = findViewById<TextView>(R.id.loading_tv)
        loadingMessage.text = message
        super.show()
    }

}