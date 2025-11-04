package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter //initialized later in onCreate
    private lateinit var dbHelper: TaskDatabaseHelper //initialized later in onCreate
    private var allTasks = listOf<Task>() //holds all tasks




    //init, set layout, UI configuration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this) //define helper here
        //connect xml views to kotlin var
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)

        //recyclerview setup
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(emptyList())
        recyclerView.adapter = adapter

        //search functionality

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allTasks //show all tasks
                } else {
                    allTasks.filter { it.title.contains(query, ignoreCase = true) }
                }
                adapter.updateList(filtered)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //handles the FAB click
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        //pull from database
        allTasks = dbHelper.getAllTasks()
        adapter.updateList(allTasks) //shows all the tasks by default
    }
}