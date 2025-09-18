package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {
    companion object {
        val tasks = mutableListOf<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Search", "User submitted: $query")
                // TODO: filter task list based on the query
                return true
            }

            override fun onQueryTextChange(newText: String?) : Boolean {
                Log.d("Search", "User typing: $newText")
                //TODO: dynamically filter task list
                return true
            }
        })
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onResume(){
        super.onResume()

        val taskListView = findViewById<LinearLayout>(R.id.taskList)

        taskListView.removeAllViews()
        for (task in tasks) {
            val taskText = TextView(this)
            taskText.text = task
            taskText.textSize = 18f
            taskListView.addView(taskText)
        }
    }
}