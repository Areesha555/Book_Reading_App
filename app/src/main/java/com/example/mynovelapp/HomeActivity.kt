package com.example.mynovelapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class HomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var interstitialAd: InterstitialAd? = null
    private var startAd: Int? = null // Nullable Int
    private var endAd: Int = 3      // Non-nullable Int



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        window.statusBarColor = resources.getColor(R.color.green, null)

        val bookmarkButton: Button = findViewById(R.id.bookmarkbtn)
        val startReading: Button = findViewById(R.id.readbtn)
        val rateUs: Button = findViewById(R.id.ratebtn)
        val readMoreNovels : Button = findViewById(R.id.readmorenovels)
        val shareApp: Button = findViewById(R.id.sharebtn)
        val privacyPolicy: Button = findViewById(R.id.privacybtn)
        val adView: AdView = findViewById(R.id.adView)

        // Load banner ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Load interstitial ad
        loadNewAd()

        rateUs.setOnClickListener {
            rateApp()

        }


        shareApp.setOnClickListener {
            shareApp()

        }

        privacyPolicy.setOnClickListener {
            privacyPolicy()

        }

        readMoreNovels.setOnClickListener{
            readMoreNovel()
        }

        startReading.setOnClickListener {
            val pageContent = loadPageContent()
            if (pageContent.isNotEmpty()) {
                val intent = Intent(this, PageActivity::class.java)
                intent.putStringArrayListExtra("PAGE_CONTENT", ArrayList(pageContent))
                showAd { startActivity(intent) }
            }
        }

        bookmarkButton.setOnClickListener {
            val intent = Intent(this, BookmarksActivity::class.java)
            showAd { startActivity(intent) }
        }
    }

    private fun loadNewAd() {
        val adRequest = AdRequest.Builder().build()
        val interstitialid = getString(R.string.interstitial_id)

        InterstitialAd.load(this, interstitialid, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    private fun showAd(onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.show(this as Activity)
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadNewAd() // Load a new ad after dismissal
                    onAdDismissed() // Execute the action after ad dismissal
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAdDismissed() // Execute the action immediately if ad fails to show
                }
            }
        } else {
            onAdDismissed() // Execute the action immediately if ad is not loaded
        }
    }

    private fun loadPageContent(): List<String> {
        return try {
            val inputStream = assets.open("output.txt")
            val content = inputStream.bufferedReader().use { it.readText() }
            val regex = "=== Page \\d+ ===".toRegex()
            val pages = regex.split(content)
            pages.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        val shareMessage = getString(R.string.share_messsage)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun privacyPolicy() {
        val privacy_policy_URL = getString(R.string.privacy_policy_URL)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacy_policy_URL))
        startActivity(intent)
    }

    private fun readMoreNovel(){

        val read_more_novels = getString(R.string.read_more_novels)

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(read_more_novels)
        )
        startActivity(intent)
    }

    private fun rateApp() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}
