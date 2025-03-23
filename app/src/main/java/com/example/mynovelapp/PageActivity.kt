package com.example.mynovelapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class PageActivity : AppCompatActivity() {

    private var pageNumber: Int = 1 // Default to Page 1
    private lateinit var pageContentTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dayButton: AppCompatImageButton
    private lateinit var layout: RelativeLayout
    private lateinit var scrollView: ScrollView
    private lateinit var starButton: AppCompatImageButton
    private var isBookmarked: Boolean = false // Variable to track the bookmark
    private var isFromBookmark: Boolean = false // Variable to track the bookmark
    private var isNightMode: Boolean = false
    private var countData: Int = 0
    private var interstitialAd: InterstitialAd? = null
    private var startAd: Int? = null // Nullable Int
    private var endAd: Int = 9      // Non-nullable Int
    var currentTextSize = 50f


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)

        window.statusBarColor = resources.getColor(R.color.green, null)
        sharedPreferences = getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)

        // Get the page number from the intent (if passed, else default to page 1)
        pageNumber = intent.getIntExtra("PAGE_NUMBER", 0)
        isFromBookmark = intent.getBooleanExtra("FromBookmark", false)
        Log.d("PageActivity", "Received PAGE_NUMBER: $pageNumber")

        // Initialize the TextView for displaying the page content
        pageContentTextView = findViewById(R.id.pageContentTextView)


        if(isFromBookmark)
            loadPageContent(pageNumber)

        // Handle the button clicks
        val backButton: AppCompatImageButton = findViewById(R.id.backbtn)
        starButton = findViewById(R.id.starbtn)
        layout = findViewById(R.id.PgmainLayout)
        scrollView = findViewById(R.id.contentScrollView)
        dayButton = findViewById(R.id.daybtn)
        var adRequest: AdRequest? = null
        var adView: AdView? = null
        adView = findViewById(R.id.adView)
        updateDayNightMode()

        // Load banner ad
        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Load interstitial ad
        loadNewAd()

        dayButton.setOnClickListener {
            isNightMode = !isNightMode // Toggle mode
            updateDayNightMode() // Update button icon based on the mode
        }

        val nextButton: AppCompatImageButton = findViewById(R.id.nextbtn)
        val pageDropdown: Spinner = findViewById(R.id.pageDropdown)
        val zoomInBtn = findViewById<ImageButton>(R.id.zoomInBtn)
        val zoomOutBtn = findViewById<ImageButton>(R.id.zoomOutBtn)





        // Fetch page titles from the output.txt
        val pages = fetchPageTitles()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pageDropdown.adapter = adapter
        if(isFromBookmark && countData == 0)
        {
            pageDropdown.setSelection(pageNumber - 1)
        }
        // Set Spinner listener to update content based on selected page
        pageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {

                if(isFromBookmark && countData >=1)
                {
                    if (pageNumber != position + 1) {
                        pageNumber = position + 1
                        loadPageContent(pageNumber)
                        checkBookmarkStatus() // Check bookmark status when the page changes
                    }
                }
                else if(!isFromBookmark)
                {
                    Log.e("DD",""+(pageNumber != position + 1))
                    if (pageNumber != position + 1) {
                        Log.e("DD",""+(pageNumber) +"" + position + 1)
                        pageNumber = position + 1
                        loadPageContent(pageNumber)
                        checkBookmarkStatus() // Check bookmark status when the page changes
                    }
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle when no item is selected (optional)
            }
        }

        backButton.setOnClickListener {
            if(startAd == null){
                startAd = 0
            }
            startAd = startAd!! + 1

            countData++
            if (pageNumber > 1) {
                pageNumber--
                loadPageContent(pageNumber)
                checkBookmarkStatus() // Check bookmark status when the page changes
                pageDropdown.setSelection(pageNumber - 1)
                if(startAd!! >= endAd){
                    showAd()
                    startAd = 0
                }
            } else {
                Toast.makeText(this, "Already on the first page", Toast.LENGTH_SHORT).show()
            }
        }

        // Toggle bookmark button icon on click
        starButton.setOnClickListener {
            isBookmarked = !isBookmarked
            if (isBookmarked) {
                saveBookmark(pageNumber)  // Save the bookmark
            } else {
                removeBookmark(pageNumber)  // Remove the bookmark
            }
            updateBookmarkIcon()  // Update the icon based on the new state
        }



        nextButton.setOnClickListener {
            if(startAd == null){
                startAd = 0
            }
            startAd = startAd!! + 1


            val pages = fetchPageTitles() // Get the total number of pages
            countData++
            if (pageNumber < pages.size) { // Ensure not exceeding total pages
                pageNumber++
                loadPageContent(pageNumber)
                checkBookmarkStatus() // Check bookmark status when the page changes
                pageDropdown.setSelection(pageNumber - 1)
                if(startAd!! >= endAd){
                    showAd()
                    startAd = 0
                }

            } else {
                Toast.makeText(this, "You're on the last page", Toast.LENGTH_SHORT).show()
            }
        }
        zoomInBtn.setOnClickListener {
            zoomIn() // Call the zoomIn method on click
        }

        zoomOutBtn.setOnClickListener {
            zoomOut() // Call the zoomOut method on click
        }

        // Check bookmark status when the activity is created
        checkBookmarkStatus()



    }

    private fun zoomIn(){
        if (currentTextSize < 60f) { // Max size limit
            currentTextSize += 2f
            pageContentTextView.textSize = currentTextSize / resources.displayMetrics.scaledDensity

        }
    }

    private fun zoomOut(){
        if (currentTextSize > 10f) { // Min size limit
            currentTextSize -= 2f
            pageContentTextView.textSize = currentTextSize / resources.displayMetrics.scaledDensity
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

    private fun showAd() {
        if (interstitialAd != null) {
            interstitialAd?.show(this as Activity)
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadNewAd() // Load a new ad after dismissal

                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {

                }
            }
        }
    }



    // Save bookmark to SharedPreferences
    private fun saveBookmark(pageNumber: Int) {
        val bookmarks = sharedPreferences.getStringSet("BOOKMARK_PAGES", mutableSetOf()) ?: mutableSetOf()
        bookmarks.add(pageNumber.toString()) // Add the page number to the bookmark list
        val editor = sharedPreferences.edit()
        editor.putStringSet("BOOKMARK_PAGES", bookmarks) // Save the updated set of bookmarks
        editor.apply()
    }




    private fun removeBookmark(pageNumber: Int) {
        val bookmarkedPages = sharedPreferences.getStringSet("BOOKMARK_PAGES", mutableSetOf()) ?: mutableSetOf()
        bookmarkedPages.remove(pageNumber.toString())  // Remove the page from the set
        sharedPreferences.edit().putStringSet("BOOKMARK_PAGES", bookmarkedPages).apply()
    }
    private fun updateBookmarkIcon() {
        if (isBookmarked) {
            starButton.setImageResource(R.drawable.star_yellow)  // Active bookmark icon
        } else {
            starButton.setImageResource(R.drawable.star_10347517)  // Inactive bookmark icon
        }
    }
    private fun checkBookmarkStatus() {
        val bookmarkedPages = sharedPreferences.getStringSet("BOOKMARK_PAGES", mutableSetOf()) ?: mutableSetOf()
        isBookmarked = bookmarkedPages.contains(pageNumber.toString())  // Check for the current page
        updateBookmarkIcon()  // Set the correct icon
    }

    // Function to load content for the selected page
    private fun loadPageContent(pageNumber: Int) {
        // Fetch the total number of pages
        val pages = fetchPageTitles()

        if (pageNumber in 1..pages.size) { // Ensure the pageNumber is within bounds
            val pageContent = getPageContentFromAssets(pageNumber)
            if (pageContent.isNotEmpty()) {
                Log.d("PageActivity", "Loading content for Page $pageNumber")
                pageContentTextView.text = pageContent
            } else {
                Log.e("PageActivity", "Error loading content for Page $pageNumber")
                pageContentTextView.text = "Error loading content for page $pageNumber."
            }
        } else {
            Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show()
        }
    }



    // Function to read content from the output.txt file in the assets folder
    private fun getPageContentFromAssets(pageNumber: Int): String {
        return try {
            val inputStream = assets.open("output.txt")
            val content = inputStream.bufferedReader().use { it.readText() }
            val regex = "=== Page \\d+ ===".toRegex()
            val pages = regex.split(content)


            if (pageNumber in 1 until pages.size) {
                Log.e("Da",""+pageNumber)
                pages[pageNumber].trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("PageActivity", "Error loading page content: ${e.message}")
            ""
        }
    }




    // Function to fetch page titles from output.txt and return as a list of pages
    private fun fetchPageTitles(): List<String> {
        val pageTitles = ArrayList<String>()
        try {
            // Open the output.txt file from the assets folder
            val inputStream = assets.open("output.txt")
            val content = inputStream.bufferedReader().use { it.readText() }

            // Split the content by the page markers (e.g., === Page 1 ===)
            val regex = "=== Page \\d+ ===".toRegex()
            val pageSections = regex.split(content)

            // Add page titles to the list
            for (i in 1 until pageSections.size) {
                pageTitles.add("Page $i")
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error fetching page titles", Toast.LENGTH_SHORT).show()
        }

        return pageTitles
    }




    private fun updateDayNightMode() {
        if (isNightMode) {
            scrollView.setBackgroundColor(resources.getColor(R.color.black))
            pageContentTextView.setBackgroundColor(resources.getColor(R.color.black))
            pageContentTextView.setTextColor(resources.getColor(R.color.white))
            dayButton.setImageResource(R.drawable.night) // Use a moon icon for night mode
        } else {
            dayButton.setImageResource(R.drawable.day) // Use a sun icon for day mode
            scrollView.setBackgroundColor(resources.getColor(R.color.white))
            pageContentTextView.setBackgroundColor(resources.getColor(R.color.white))
            pageContentTextView.setTextColor(resources.getColor(R.color.black))
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.edit().putInt("LAST_VIEWED_PAGE", pageNumber).apply()  // Save the last page
    }

}


