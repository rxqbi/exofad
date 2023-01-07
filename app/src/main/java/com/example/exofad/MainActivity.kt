package com.example.exofad

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

import android.widget.Button
import android.widget.TextView

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.exofad.databinding.ActivityMainBinding
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions


import java.util.Locale


private const val START_LEVEL = 1

class MainActivity : AppCompatActivity(), OnUserEarnedRewardListener {

    private var currentLevel: Int = 0
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private lateinit var nextLevelButton: Button
    private lateinit var levelTextView: TextView
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(
            this
        ) { }
        // Load the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        loadRewardedInterstitialAd()

        // Create the next level button, which tries to show an interstitial when clicked.
        nextLevelButton = binding.nextLevelButton
        nextLevelButton.isEnabled = false
        nextLevelButton.setOnClickListener { showRewardedInterstitial() }

        levelTextView = binding.level
        // Create the text view to show the level number.
        currentLevel = START_LEVEL

    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Log.d(TAG, "User earned reward.")
        // TODO: Reward the user!
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }

    private fun loadRewardedInterstitialAd() {
        RewardedInterstitialAd.load(this, getString(R.string.R_interstitial_ad),
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    Log.d(TAG, "Ad was loaded.")
                    rewardedInterstitialAd = ad
                    nextLevelButton.isEnabled = true
                    val options = ServerSideVerificationOptions.Builder()
                        .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                        .build()
                    rewardedInterstitialAd?.setServerSideVerificationOptions(options)
                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT)
                        .show()
                    ad.setFullScreenContentCallback(
                        object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.")
                        }
                        override fun onAdDismissedFullScreenContent() {
                            // Called when fullscreen content is dismissed.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            rewardedInterstitialAd = null
                            Log.d(TAG, "The ad was dismissed.")
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when fullscreen content failed to show.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            rewardedInterstitialAd = null
                            Log.d(TAG, "The ad failed to show.")
                        }
                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            Log.d(TAG, "The ad was shown.")
                        }
                    })
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i(TAG, loadAdError.message)
                    rewardedInterstitialAd = null
                    nextLevelButton.isEnabled = true
                    val error: String = String.format(
                        Locale.ENGLISH,
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error: $error", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }

    private fun showRewardedInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.show(/* Activity */ this, /* OnUserEarnedRewardListener */ this)
                Log.d(TAG, "User earned the reward.")

        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
            goToNextLevel()
        }
    }

    private fun goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        "Level ${++currentLevel}".also { levelTextView.text = it }
        if (rewardedInterstitialAd == null) {
            loadRewardedInterstitialAd()
        }
    }
}
