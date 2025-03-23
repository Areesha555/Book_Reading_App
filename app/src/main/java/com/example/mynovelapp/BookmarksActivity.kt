package com.example.mynovelapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class BookmarksActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var bookmarkedPages: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        sharedPreferences = getSharedPreferences("Bookmarks", MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerView)
        window.statusBarColor = resources.getColor(R.color.green, null)
        var adRequest: AdRequest? = null
        var adView: AdView? = null
        adView = findViewById(R.id.adView)


        // Load bookmarks when the activity is created
        val bookmarkedPages = loadBookmarks()

        // Set up RecyclerView
        bookmarkAdapter = BookmarkAdapter(bookmarkedPages) { pageNumber ->
            // Open the selected page content in PageActivity
            val intent = Intent(this, PageActivity::class.java)
            intent.putExtra("PAGE_NUMBER", pageNumber)
            intent.putExtra("FromBookmark", true)
            startActivity(intent)
            Log.d("BookmarksActivity", "Opening Page $pageNumber")
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = bookmarkAdapter

        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest!!)
    }

    private fun loadBookmarks(): List<Int> {
        val bookmarks = sharedPreferences.getStringSet("BOOKMARK_PAGES", mutableSetOf()) ?: mutableSetOf()
        return bookmarks.mapNotNull {
            it.toIntOrNull()
        }.sorted()
    }



}
