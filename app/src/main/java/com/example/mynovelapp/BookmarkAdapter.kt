package com.example.mynovelapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.RecyclerView

class BookmarkAdapter(
    private val bookmarkedPages: List<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_bookmarks, parent, false)
        return BookmarkViewHolder(view)


    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val pageNumber = bookmarkedPages[position]
        holder.bind(pageNumber)
    }

    override fun getItemCount(): Int {
        return bookmarkedPages.size
    }


    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pageTitle: TextView = itemView.findViewById(R.id.pageTitle)

        fun bind(pageNumber: Int) {
            pageTitle.text = "Page $pageNumber"
            itemView.setOnClickListener {
                onItemClick(pageNumber)
            }
        }
    }
}
