package com.aghourservices

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.aghourservices.cache.UserInfo
import com.aghourservices.firebase_analytics.Event
import com.aghourservices.interfaces.ActivityFragmentCommunicator
import com.aghourservices.user.SignInActivity

open class BaseFragment : Fragment(), ActivityFragmentCommunicator {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Event.sendScreenName(this::class.simpleName.toString())
    }
    override fun onBackPressed(): Boolean {
        return false
    }

    fun shareApp() {
        Event.sendFirebaseEvent("Share", "")
        val shareText = getString(R.string.shareText)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "مشاركة التطبيق"))
    }

    fun rateApp() {
        Event.sendFirebaseEvent("Rate", "")
        val url = "https://play.google.com/store/apps/details?id=com.aghourservices"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun facebook() {
        Event.sendFirebaseEvent("Facebook_Page", "")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb:/page/110004384736318")))
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.facebook.com/aghourservices")
                )
            )
        }
    }

    private fun logOut() {
        Event.sendFirebaseEvent("Sign_Out", "")
        UserInfo().clearUserData(requireActivity())
        startActivity(Intent(requireActivity(), SignInActivity::class.java))
    }

    fun showOnCloseDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setTitle(R.string.title)
        alertDialogBuilder.setMessage(R.string.message)
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_round)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton(R.string.positiveButton) { _, _ ->
            logOut()
            Event.sendFirebaseEvent("ALERT_LOGOUT_ACTION", "")
        }
        alertDialogBuilder.setNegativeButton(R.string.negativeButton) { _, _ ->
            Event.sendFirebaseEvent("ALERT_STAY_ACTION", "")
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
    }

}