package com.example.dropbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 123
    private val PICK_IMAGE_REQUEST_CODE = 456
    private lateinit var dbxClient: DbxClientV2
    private lateinit var btn : FloatingActionButton
    private lateinit var btnFloatingNext : FloatingActionButton
    private lateinit var rv : RecyclerView
    private val ACCESS_TOKEN = "sl.Bjz1LNqah-YygDEh4ccfgL6J0xG7cAUwtqyo26VBBpXlTlQCpaa4JRJSh76dS0m4fYCYTqvSgrFO2jcqKL1plc8X4PK6hty4CqnnQ2rmd6UmQF5fLDdhcZbXMFh4rd5FOvpeY5VSRMQn6-G-c0yc4g0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.btn)
        rv = findViewById(R.id.rv)
        btnFloatingNext = findViewById(R.id.btnFloatingNext)

        btnFloatingNext.setOnClickListener{
            startActivity(Intent(this@MainActivity,In_app_purchase::class.java))
        }

        // get file
        val requestConfig = DbxRequestConfig.newBuilder("Rushibhai").build()
        dbxClient = DbxClientV2(requestConfig, ACCESS_TOKEN)

        rv.layoutManager = LinearLayoutManager(this)
        fetchAndDisplayImages()

        // upload file
        btn.setOnClickListener {
            requestPermission()
        }
    }

    // get file
    private fun fetchAndDisplayImages() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val imageUrls = mutableListOf<String>()
                val filesList = dbxClient.files().listFolder("/your-destination-path/").entries

                for (file in filesList) {
                    if (file.name.endsWith(".jpg") || file.name.endsWith(".png")) {
                        val imageUrl = dbxClient.files().getTemporaryLink(file.pathDisplay).link
                        imageUrls.add(imageUrl)
                    }
                }

                runOnUiThread {
                    val imageAdapter = Custom_Adapter( imageUrls)
                    rv.adapter = imageAdapter
                    imageAdapter.notifyDataSetChanged()
                    Log.e("adapter","$imageAdapter")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("oo","$e")
            }
        }
    }

    // upload file
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openImagePicker()
            }
        } else {
            openImagePicker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data ?: return

            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImageUri, filePathColumn, null, null, null)
            cursor?.let {
                it.moveToFirst()
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val filePath = it.getString(columnIndex)
                it.close()
                uploadToDropbox(filePath)
            }
        }
    }

    private fun uploadToDropbox(filePath: String) {

        val requestConfig = DbxRequestConfig.newBuilder("Rushibhai").build()
        dbxClient = DbxClientV2(requestConfig, ACCESS_TOKEN)

        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val remotePath = "/your-destination-path/${file.name}"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                dbxClient.files().uploadBuilder(remotePath)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream)
            } catch (e: Exception) {
               Log.e("dd","$e")
            } finally {
                inputStream.close()
            }
        }
    }
}