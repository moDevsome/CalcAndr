package com.outhier.calcandr

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog

class Utilities(private val context: Context) {

    public fun alert(message: String) {

        val confirmDialog = AlertDialog.Builder(this.context);
        confirmDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->

            dialog.dismiss();

        });

        confirmDialog.setMessage(message.toString());
        confirmDialog.show();

    }

    public fun traceout(content: String, type: String?) {

        when(type) {
            "ERROR" -> Log.e("[Calcandro ERROR]", content)
            "INFO" -> Log.i("[Calcandro INFO]", content)
            else -> Log.i("[CalcandroINFO]", content)
        }

    }

}