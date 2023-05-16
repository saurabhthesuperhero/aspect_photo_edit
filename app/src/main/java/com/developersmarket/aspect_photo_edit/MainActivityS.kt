package com.developersmarket.aspect_photo_edit

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivityS : AppCompatActivity() {
    lateinit var id_imageview: ImageView
    lateinit var id_aspect_ratio: ImageView
    lateinit var id_download: ImageView
    private var selectedAspectRatioOption: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        id_imageview = findViewById(R.id.id_imageview)
        id_aspect_ratio = findViewById(R.id.id_aspect_ratio)
        id_download = findViewById(R.id.id_download)

        val imageurl = "https://images.unsplash.com/photo-1683997941376-d5bbecbcdae7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=687&q=80"
        Glide.with(this).load(imageurl).into(id_imageview)

        id_download.setOnClickListener {
            downloadImageInternal()
        }

        checkPermissions()

        id_aspect_ratio.setOnClickListener {
            showBottomSheetForAspectRatio()
        }
    }

    private fun downloadImageInternal() {
        val bitmap = getBitmapFromView(id_imageview)
        val fileName = generateFileName(selectedAspectRatioOption)

        saveBitmapToFile(
                bitmap,
                fileName
        )
    }

    private fun getBitmapFromView(view: ImageView): Bitmap {
        val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun generateFileName(aspectRatioOption: String): String {
        val randomName = "image_${System.currentTimeMillis()}_"
        return "${aspectRatioOption}${randomName}.jpg"
    }

    @OptIn(DelicateCoroutinesApi::class) private fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            // Create the AspectRatioApp directory inside the Download directory if it doesn't exist
            val appDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AspectRatioApp")
            if (!appDirectory.exists()) {
                appDirectory.mkdir()
            }

            // Save the Bitmap to a file inside the AspectRatioApp directory
            val imageFile = File(appDirectory, fileName)
            try {
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                // Trigger media scan for the saved image file
                MediaScannerConnection.scanFile(
                        this@MainActivityS,
                        arrayOf(imageFile.absolutePath),
                        null,
                        null
                )
                withContext(Dispatchers.Main) {
                    onImageDownloaded(true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onImageDownloaded(false)
                }
            }
        }

    }

    private fun onImageDownloaded(success: Boolean) {
        runOnUiThread {
            if (success) {
                Toast.makeText(
                        this,
                        "Image downloaded successfully",
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                        this,
                        "Failed to download image",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkPermissions() {
        PermissionX.init(this).permissions(Manifest.permission.MANAGE_EXTERNAL_STORAGE).onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                    deniedList,
                    "Core fundamental are based on these permissions",
                    " OK",
                    "Cancel"
            )
        }.onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
            )
        }.request { allGranted, _, deniedList ->
            if (!allGranted) {
                Toast.makeText(
                        this,
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showBottomSheetForAspectRatio() {
        val bottomSheetView = layoutInflater.inflate(
                R.layout.bottom_sheet,
                null
        )
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        setupBottomSheetOptions(bottomSheetDialog,bottomSheetView)
    }

    private fun setupBottomSheetOptions(bottomSheetDialog: BottomSheetDialog,bottomSheetView: View) {

        val option_facebook_1_1 = bottomSheetView.findViewById<TextView>(R.id.option_facebook_1_1)
        val option_facebook_4_5 = bottomSheetView.findViewById<TextView>(R.id.option_facebook_4_5)
        val option_instagram_story_9_16 = bottomSheetView.findViewById<TextView>(R.id.option_instagram_story_9_16)
        val option_instagram_1_1 = bottomSheetView.findViewById<TextView>(R.id.option_instagram_1_1)
        val option_twitter_16_9 = bottomSheetView.findViewById<TextView>(R.id.option_twitter_16_9)

        option_facebook_1_1.setOnClickListener {
            setAspectRatio(
                    1080,
                    1080,
                    "facebook_1_1"
            )
            bottomSheetDialog.dismiss()
        }

        option_facebook_4_5.setOnClickListener {
            setAspectRatio(
                    1200,
                    1500,
                    "facebook_4_5"
            )
            bottomSheetDialog.dismiss()
        }

        option_instagram_story_9_16.setOnClickListener {
            setAspectRatio(
                    1080,
                    1920,
                    "instagram_story_9_16"
            )
            bottomSheetDialog.dismiss()
        }

        option_twitter_16_9.setOnClickListener {
            setAspectRatio(
                    1200,
                    675,
                    "twitter_16_9"
            )
            bottomSheetDialog.dismiss()
        }

        option_instagram_1_1.setOnClickListener {
            setAspectRatio(
                    1080,
                    1080,
                    "instagram_1_1"
            )
            bottomSheetDialog.dismiss()
        }
    }

    private fun setAspectRatio(width: Int, height: Int, option: String) {
        val layoutParams = id_imageview.layoutParams as LinearLayout.LayoutParams
        layoutParams.width = width
        layoutParams.height = height
        id_imageview.layoutParams = layoutParams
        id_imageview.setBackgroundResource(R.color.white)
        selectedAspectRatioOption = option
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}