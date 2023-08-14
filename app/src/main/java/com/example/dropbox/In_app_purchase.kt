package com.example.dropbox

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

@Suppress("DEPRECATION")
class In_app_purchase : AppCompatActivity() {

private lateinit var btn_buynow : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_purchase)

        supportActionBar!!.hide()

        btn_buynow = findViewById(R.id.btn_buynow)

        val skuList = ArrayList<String>()
        skuList.add("android.test.purchased")

        val purchasesUpdatedListener = PurchasesUpdatedListener{
            billingResult, purchses ->
        }

        val billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases().build()

        btn_buynow.setOnClickListener{
            billingClient.startConnection(object : BillingClientStateListener{
                override fun onBillingServiceDisconnected() {
                    Toast.makeText(this@In_app_purchase, "Disconnected", Toast.LENGTH_SHORT).show()
                }

                override fun onBillingSetupFinished(billingResult : BillingResult) {
                    Toast.makeText(this@In_app_purchase, "Connected", Toast.LENGTH_SHORT).show()
                    if(billingResult.responseCode == BillingClient.BillingResponseCode.OK){

                        val params = SkuDetailsParams.newBuilder()
                        params.setSkusList(skuList)
                            .setType(BillingClient.SkuType.INAPP)

                        billingClient.querySkuDetailsAsync(params.build()){
                                _, skuDetailsList ->

                            for (skuDetails in skuDetailsList!!) {
                                val flowPurchase = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
                                val responseCode = billingClient.launchBillingFlow(this@In_app_purchase,flowPurchase).responseCode
                            }
                        }
                    }
                }
            })
        }
    }
}