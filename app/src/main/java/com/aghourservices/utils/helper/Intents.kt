package com.aghourservices.utils.helper

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.aghourservices.R
import com.aghourservices.ui.main.activity.SignInActivity
import com.aghourservices.ui.main.cache.UserInfo.clearUserData
import java.io.File
import java.io.FileOutputStream


object Intents {

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(projection[0])
        val filePath = cursor?.getString(columnIndex!!)
        cursor?.close()
        return filePath
    }

    fun compressFile(context: Context, file: File): File? {
        return try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.path, o)

            val REQUIRED_SIZE = 100
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val selectedBitmap = BitmapFactory.decodeFile(file.path, o2)

            val compressedFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
            compressedFile.createNewFile()
            val outputStream = FileOutputStream(compressedFile)

            var quality = 100
            var compressedSize = -1
            while (compressedSize > 100 * 1024 || compressedSize == -1) {
                selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedSize = compressedFile.length().toInt()
                quality -= 5
            }

            outputStream.close()
            compressedFile
        } catch (e: Exception) {
            null
        }
    }

    fun shareApp(context: Context) {
        Event.sendFirebaseEvent("Share_App", "")
        val shareText = context.getString(R.string.shareText)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "مشاركة التطبيق"))
    }

    fun rateApp(context: Context) {
        Event.sendFirebaseEvent("Rate", "")
        val url = "https://play.google.com/store/apps/details?id=com.aghourservices"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun facebook(context: Context) {
        Event.sendFirebaseEvent("Facebook_Page", "")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb:/page/110004384736318")))
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.facebook.com/aghourservices")
                )
            )
        }
    }

    fun whatsApp(context: Context, number: String) {
        Event.sendFirebaseEvent("Whats_App", "")
        val url = "https://api.whatsapp.com/send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    fun gmail(context: Context) {
        Event.sendFirebaseEvent("Gmail_App", "")
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(context.getString(R.string.gmail_uri))
        }
        context.startActivity(emailIntent)
    }

    private fun logOut(context: Context) {
        Event.sendFirebaseEvent("Sign_Out", "")
        clearUserData(context)
        context.startActivity(Intent(context, SignInActivity::class.java))
        (context as AppCompatActivity).finishAffinity()
    }

    fun copyNews(
        description: String,
        itemView: View
    ) {
        val clipboardManager =
            itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("News", description)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(itemView.context, "تم نسخ الخبر", Toast.LENGTH_LONG).show()
    }

    fun copyFirm(
        name: String,
        address: String,
        description: String,
        phoneNumber: String,
        itemView: View
    ) {
        val clipboardManager =
            itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Firms",
            "$name \n $address \n $description \n $phoneNumber"
        )
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(itemView.context, "تم نسخ المحتوى", Toast.LENGTH_LONG).show()
    }

    fun shareFirm(
        name: String,
        address: String,
        description: String,
        phoneNumber: String,
        itemView: View
    ) {
        val eventName = "Share_${name}"
        Event.sendFirebaseEvent(eventName, "")
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "$name \n $address \n $description \n $phoneNumber \n ${itemView.context.getString(R.string.aghour_share_content)}"
            )
            type = "text/plain"
        }
        itemView.context.startActivity(Intent.createChooser(sendIntent, "شارك بواسطة.."))
    }

    fun shareNews(article: String, itemView: View) {
        Event.sendFirebaseEvent("Share_news", "")
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "$article\n ${itemView.context.getString(R.string.aghour_share_content)}"
            )
            type = "text/plain"
        }
        itemView.context.startActivity(Intent.createChooser(shareIntent, "شارك الخبر.."))
    }

    fun showOnCloseDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        alertDialogBuilder.setTitle(R.string.title)
        alertDialogBuilder.setMessage(R.string.message)
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_round)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton(R.string.positiveButton) { _, _ ->
            logOut(context)
        }
        alertDialogBuilder.setNegativeButton(R.string.negativeButton) { _, _ ->
            Event.sendFirebaseEvent("STAY_ACTION", "")
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun checkTheme(context: Context) {
        when (ThemePreference(context).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}