package com.dev_yogesh.googlepayintegration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dev_yogesh.googlepayintegration.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val GPAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
    var uri: Uri? = null
    var approvalRefNo: String? = null
    var payerName: String? = null
    var UpiId: String? = null
    var msgNote: String? = null
    var sendAmount: String? = null
    var status: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        setUpi()
        listener()
    }

    private fun setUpi() = with(binding){
        //initialising default value
       // upiId.setText("bansal.yogesh940@okaxis")

    }

    private fun listener() = with(binding){
        pay.setOnClickListener {
            payerName = name.text.toString()
            UpiId = upiId.text.toString()
            msgNote = transactionNote.text.toString()
            sendAmount = amount.text.toString()
            if (payerName != "" && UpiId != "" && msgNote != "" && sendAmount != "") {
                uri = getUpiPaymentUri(
                    payerName!!,
                    UpiId!!,
                    msgNote!!,
                    sendAmount!!
                )
                payWithGpay(GPAY_PACKAGE_NAME)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Fill all above details and try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getUpiPaymentUri(name: String, upiId: String, note: String, amount: String): Uri? {
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()
    }

    private fun payWithGpay(packageName: String) {
        if (isAppInstalled(this, packageName)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(packageName)
            startActivityForResult(intent, 0)
        } else {
            Toast.makeText(
                this@MainActivity,
                "Google pay is not installed. Please install and try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isAppInstalled(context: Context, packageName: String?): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            status = data.getStringExtra("Status")!!.toLowerCase()
            approvalRefNo = data.getStringExtra("txnRef")
        }
        if (RESULT_OK == resultCode && status == "success") {
            Toast.makeText(
                this@MainActivity,
                "Transaction successful. $approvalRefNo",
                Toast.LENGTH_SHORT
            ).show()
            binding.status.apply {
                text = getString(R.string.transaction_successful).plus(sendAmount)
                setTextColor(Color.GREEN)
                isVisible= true
            }
        } else {
            Toast.makeText(
                this@MainActivity,
                "Transaction cancelled or failed please try again.",
                Toast.LENGTH_SHORT
            ).show()
            binding.status.apply {
                text = getString(R.string.transaction_failed).plus(sendAmount)
                setTextColor(Color.RED)
                isVisible= true
            }

        }
    }
}