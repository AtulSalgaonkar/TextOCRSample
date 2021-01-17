package com.lm.textocrsample

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_text_ocr_demo.*
import java.io.FileDescriptor
import java.io.IOException


class TextOCRDemoActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null
    private val TAG = "TextOCRDemoActivity"

    // ML Kit text OCR Url: https://developers.google.com/ml-kit/vision/text-recognition/android#kotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_ocr_demo)

        path_select_view.setOnClickListener {
            if (!checkStoragePermission()) {
                requestPermissions(listOf(storage_permission).toTypedArray(), 100)
            } else {
                pickImage()
            }
        }
        val recognizer = TextRecognition.getClient()

        detect_btn.setOnClickListener() {
            bitmap?.let {
                val image = InputImage.fromBitmap(it, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        // textBlocks -> will return list of block of detected text
                        // lines -> will return list of detected lines
                        // elements -> will return list of detected words
                        // boundingBox -> will return rectangle box area in bitmap
                        Toast.makeText(this, visionText.text, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                    }
            }
            if (bitmap == null) Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT)
                .show()

        }

    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    private fun checkStoragePermission(): Boolean {
        return checkSelfPermission(storage_permission) == PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (isAllPermissionGranted(permissions)) {
                    pickImage()
                } else {
                    Toast.makeText(this, "Please grant storage permission!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        data?.data?.let {
                            path_et.setText(it.path)
                            Log.e(TAG, "Uri: $it")
                            bitmap = null
                            bitmap = getBitmapFromUri(it);
                            Glide.with(text_image)
                                .load(bitmap)
                                .into(text_image)
                        }

                    }
                    RESULT_CANCELED -> {
                        bitmap = null
                        Toast.makeText(this, "Please select valid image!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun isAllPermissionGranted(permissions: Array<out String>): Boolean {
        permissions.forEach {
            if (checkSelfPermission(it) != PERMISSION_GRANTED) return false
        }
        return true
    }

    companion object {
        const val storage_permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val PICK_IMAGE = 101
    }

    /**
     * credits: https://stackoverflow.com/a/21517011
     */
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }


}