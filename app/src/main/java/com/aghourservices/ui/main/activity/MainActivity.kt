package com.aghourservices.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.aghourservices.R
import com.aghourservices.databinding.ActivityMainBinding
import com.aghourservices.databinding.BottomSheetBinding
import com.aghourservices.ui.fragment.CategoriesFragmentDirections
import com.aghourservices.utils.ads.Interstitial
import com.aghourservices.utils.ads.RewardAd
import com.aghourservices.utils.helper.Event.Companion.sendFirebaseEvent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.myLooper()!!)
    private val interstitial = Interstitial()
    private val rewardAd = RewardAd()
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rewardAd.loadRewardedAd(this)
        val mainNavController = setupNavController()
        checkExtras(mainNavController)
        floatActionButton()
        rewardAd()
        adView()
        inAppRating()
    }

    private fun inAppRating() {
        reviewManager = ReviewManagerFactory.create(this)
        val request: Task<ReviewInfo> = reviewManager!!.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            }
        }
//        TODO: Just for testing
//        binding.button.setOnClickListener {
//            val flow = reviewManager!!.launchReviewFlow(
//                this@MainActivity,
//                reviewInfo!!
//            )
//            flow.addOnCompleteListener { }
//        }
    }

    private fun checkExtras(mainNavController: NavController) {
        val newsTopic = getString(R.string.news_topic)
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                if (key == "from" && extras.get(key).toString().contains(newsTopic)) {
                    mainNavController.navigate(
                        CategoriesFragmentDirections.actionCategoriesFragmentToNewsFragment()
                    )
                }
            }
        }

        /** Check for notification foreground **/
        if (intent.getStringExtra("body") != null) {
            mainNavController.navigate(
                CategoriesFragmentDirections.actionCategoriesFragmentToNewsFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 120000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun adView() {
        runnable = Runnable { interstitial.load(this@MainActivity) }
        handler.post(runnable)
    }

    private fun rewardAd() {
        binding.supportApp.setOnClickListener {
            sendFirebaseEvent("Support_App", "")
            rewardAd.showAd(this)
            binding.supportApp.isVisible = false
        }
    }

    private fun setupNavController(): NavController {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.show()

        val bottomNavView = binding.bottomView
        bottomNavView.background = null
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)

        /** Show the Up button in the action bar. **/
        NavigationUI.setupActionBarWithNavController(this, navController)

        return navController
    }

    private fun floatActionButton() {
        binding.floatingActionButton.setOnClickListener {
            val binding = BottomSheetBinding.inflate(layoutInflater)

            val bottomSheetDialog = BottomSheetDialog(this).apply {
                setContentView(binding.root)
                setCancelable(true)
                show()
            }

            binding.addDataBtn.setOnClickListener {
                startActivity(Intent(this, AddDataActivity::class.java))
                bottomSheetDialog.dismiss()
            }
            binding.addArticleBtn.setOnClickListener {
                startActivity(Intent(this, AddArticleActivity::class.java))
                bottomSheetDialog.dismiss()
            }
            binding.dismissSheet.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        }
    }

    override fun setTitle(title: CharSequence?) {
        binding.toolBarTv.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingActivity -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            Navigation.findNavController(this@MainActivity, R.id.fragmentContainerView)
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }
}