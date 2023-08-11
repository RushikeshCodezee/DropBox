package com.example.dropbox

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class In_app_purchase : AppCompatActivity() {

private lateinit var btn_buynow : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_purchase)

        supportActionBar!!.hide()

        btn_buynow = findViewById(R.id.btn_buynow)

        val purchasesUpdatedListener = PurchasesUpdatedListener{
            billingResult, purchses ->
        }

        var billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases().build()

        btn_buynow.setOnClickListener{
          //  billingClient.startConnection()
        }

    }
}